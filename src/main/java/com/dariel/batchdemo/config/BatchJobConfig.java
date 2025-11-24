package com.dariel.batchdemo.config;

import com.dariel.batchdemo.domain.Customer;
import com.dariel.batchdemo.monitoring.ChunkLoggingListener;
import com.dariel.batchdemo.processing.CustomerProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BatchJobConfig - This is where we configure our Spring Batch job.
 * 
 * Spring Batch follows a simple pattern:
 * 1. READ - Read data from a source (CSV file, database, etc.)
 * 2. PROCESS - Transform/validate each item
 * 3. WRITE - Write processed items to a destination (database, file, etc.)
 * 
 * Items are processed in "chunks" (batches) for efficiency.
 * For example, with chunk size 25: read 25 items, process them, write them all at once.
 */
@Configuration
@EnableBatchProcessing
public class BatchJobConfig {

    // How many items to process before writing to database
    // Larger chunks = fewer database writes = faster, but more memory used
    private static final int CHUNK_SIZE = 25;

    /**
     * Defines the main job that will run.
     * A job is made up of one or more steps.
     */
    @Bean
    public Job customerJob(JobRepository jobRepository, Step processStep) {
        return new JobBuilder("customerJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Allows running the job multiple times
                .start(processStep) // Start with our single step
                .build();
    }

    /**
     * Defines a step in the job.
     * A step = READ + PROCESS + WRITE
     * 
     * This step:
     * 1. Reads customers from CSV file
     * 2. Processes each customer (validates and cleans)
     * 3. Writes valid customers to database
     */
    @Bean
    public Step processStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<Customer> customerReader,
                           CustomerProcessor customerProcessor,
                           JdbcBatchItemWriter<Customer> customerWriter,
                           ChunkLoggingListener chunkLoggingListener) {
        return new StepBuilder("processStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager) // Process 25 items at a time
                .reader(customerReader)      // Step 1: READ from CSV
                .processor(customerProcessor) // Step 2: PROCESS (validate & clean)
                .writer(customerWriter)       // Step 3: WRITE to database
                .listener(chunkLoggingListener) // Log progress for each chunk
                .build();
    }

    /**
     * READER: Reads customer data from CSV file.
     * 
     * Spring Batch will:
     * - Read the CSV file line by line
     * - Skip the header row (line 1)
     * - Map each row to a Customer object
     * - Pass each Customer to the processor
     */
    @Bean
    public FlatFileItemReader<Customer> customerReader(
            @Value("classpath:data/customers.csv") Resource csvFile) {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("customerReader")
                .resource(csvFile) // The CSV file to read
                .linesToSkip(1) // Skip header row
                .delimited() // CSV format (comma-separated)
                .delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
                .names("id", "firstName", "lastName", "email", "country", "purchaseAmount") // CSV column names
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {{
                    setTargetType(Customer.class); // Map to Customer object
                }})
                .build();
    }

    /**
     * PROCESSOR: Validates and cleans customer data.
     * 
     * Spring Batch calls this for each Customer read from CSV.
     * - If returns null: record is skipped (not written)
     * - If returns Customer: record is written to database
     */
    @Bean
    public CustomerProcessor customerProcessor() {
        return new CustomerProcessor();
    }

    /**
     * WRITER: Writes processed customers to database.
     * 
     * Spring Batch will:
     * - Collect processed customers in chunks (25 at a time)
     * - Write all 25 to database in one batch operation (efficient!)
     * - This is much faster than writing one at a time
     */
    @Bean
    public JdbcBatchItemWriter<Customer> customerWriter(DataSource dataSource) {
        // SQL to insert customer into database
        // :id, :firstName, etc. are placeholders that Spring Batch fills in
        String sql = "INSERT INTO customers(id, first_name, last_name, email, country, purchase_amount) " +
                     "VALUES (:id, :firstName, :lastName, :email, :country, :purchaseAmount)";
        
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource) // Database connection
                .sql(sql) // SQL statement
                .beanMapped() // Automatically map Customer properties to SQL placeholders
                .build();
    }

    /**
     * Listener that logs progress for each chunk processed.
     * This helps us see what's happening during the batch job.
     */
    @Bean
    public ChunkLoggingListener chunkLoggingListener() {
        return new ChunkLoggingListener();
    }
}
