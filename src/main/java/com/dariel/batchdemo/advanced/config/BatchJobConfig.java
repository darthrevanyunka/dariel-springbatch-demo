package com.dariel.batchdemo.advanced.config;

import com.dariel.batchdemo.advanced.domain.CountryStatistics;
import com.dariel.batchdemo.advanced.domain.Customer;
import com.dariel.batchdemo.advanced.monitoring.ChunkLoggingListener;
import com.dariel.batchdemo.advanced.monitoring.DemoJobExecutionListener;
import com.dariel.batchdemo.advanced.monitoring.DemoStepExecutionListener;
import com.dariel.batchdemo.advanced.processing.CountryStatisticsProcessor;
import com.dariel.batchdemo.advanced.processing.CountryStatisticsReader;
import com.dariel.batchdemo.advanced.processing.CustomerProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BatchJobConfig - Advanced Spring Batch demo with database integration and aggregation.
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
public class BatchJobConfig {

    // How many items to process before writing to database
    // Larger chunks = fewer database writes = faster, but more memory used
    private static final int CHUNK_SIZE = 25;

    // ============================================================================
    // JOB DEFINITION - High-level overview
    // ============================================================================

    /**
     * Defines the main job that will run.
     * A job is made up of one or more steps.
     * 
     * This job has TWO steps:
     * 1. processStep - Reads CSV, processes customers, writes to database
     * 2. aggregateStep - Reads customers from database, aggregates by country, writes statistics
     */
    @Bean
    public Job customerJob(JobRepository jobRepository, 
                          Step processStep, 
                          Step aggregateStep,
                          DemoJobExecutionListener jobExecutionListener) {
        return new JobBuilder("customerJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Allows running the job multiple times
                .listener(jobExecutionListener) // Log job start/end with visual formatting
                .start(processStep) // Step 1: Process customers from CSV
                .next(aggregateStep) // Step 2: Aggregate customers by country
                .build();
    }

    // ============================================================================
    // STEP 1: PROCESS CUSTOMERS FROM CSV
    // ============================================================================

    /**
     * STEP 1: Process customers from CSV file.
     * 
     * This step:
     * 1. Reads customers from CSV file
     * 2. Processes each customer (validates and cleans)
     * 3. Writes valid customers to database
     * 
     * A step = READ + PROCESS + WRITE
     */
    @Bean
    public Step processStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<Customer> customerReader,
                           CustomerProcessor customerProcessor,
                           JdbcBatchItemWriter<Customer> customerWriter,
                           ChunkLoggingListener chunkLoggingListener,
                           DemoStepExecutionListener stepExecutionListener) {
        return new StepBuilder("processStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager) // Process 25 items at a time
                .reader(customerReader)      // Step 1: READ from CSV
                .processor(customerProcessor) // Step 2: PROCESS (validate & clean)
                .writer(customerWriter)       // Step 3: WRITE to database
                .listener(chunkLoggingListener) // Log progress for each chunk
                .listener(stepExecutionListener) // Log step start/end with visual formatting
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

    // ============================================================================
    // STEP 2: AGGREGATE CUSTOMERS BY COUNTRY
    // ============================================================================

    /**
     * STEP 2: Aggregate customers by country and calculate statistics.
     * 
     * This step demonstrates:
     * - Reading from database (not just CSV)
     * - Aggregating data (grouping customers by country)
     * - Calculating statistics (count, total revenue, average)
     * - Writing aggregated results to a new table
     * 
     * This step:
     * 1. Reads all customers from the database
     * 2. Groups them by country and calculates statistics
     * 3. Processes the statistics (filters, rounds values)
     * 4. Writes country statistics to database
     */
    @Bean
    public Step aggregateStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             ItemStreamReader<CountryStatistics> countryStatisticsReader,
                             CountryStatisticsProcessor countryStatisticsProcessor,
                             FlatFileItemWriter<CountryStatistics> countryStatisticsWriter,
                             ChunkLoggingListener chunkLoggingListener,
                             DemoStepExecutionListener stepExecutionListener) {
        return new StepBuilder("aggregateStep", jobRepository)
                .<CountryStatistics, CountryStatistics>chunk(10, transactionManager) // Process 10 countries at a time
                .reader(countryStatisticsReader)      // Read and aggregate customers by country
                .processor(countryStatisticsProcessor) // Process statistics (filter, round)
                .writer(countryStatisticsWriter)       // Write statistics to CSV file
                .listener(chunkLoggingListener)        // Log progress for each chunk
                .listener(stepExecutionListener)       // Log step start/end with visual formatting
                .build();
    }

    /**
     * READER: Reads customers from database and aggregates them by country.
     * 
     * This is more advanced than the CSV reader because it:
     * - Reads from a database (not a file)
     * - Performs aggregation logic (groups by country)
     * - Calculates statistics (count, sum, average)
     * - Transforms data structure (Customer -> CountryStatistics)
     */
    @Bean
    public ItemStreamReader<CountryStatistics> countryStatisticsReader(DataSource dataSource) {
        return new CountryStatisticsReader(dataSource);
    }

    /**
     * PROCESSOR: Processes aggregated country statistics.
     * 
     * - Filters out countries with too few customers
     * - Rounds monetary values for reporting
     * - Applies business rules
     */
    @Bean
    public CountryStatisticsProcessor countryStatisticsProcessor() {
        return new CountryStatisticsProcessor();
    }

    /**
     * WRITER: Writes country statistics to CSV file.
     * 
     * Writes aggregated statistics (not raw customer data) to a CSV file.
     * Output file: country-statistics.csv
     */
    @Bean
    public FlatFileItemWriter<CountryStatistics> countryStatisticsWriter(
            @Value("file:country-statistics.csv") WritableResource outputFile) {
        return new FlatFileItemWriterBuilder<CountryStatistics>()
                .name("countryStatisticsWriter")
                .resource(outputFile)
                .delimited()
                .delimiter(",")
                .names(new String[]{"country", "customerCount", "totalRevenue", "averagePurchaseAmount"})
                .headerCallback(writer -> writer.write("country,customerCount,totalRevenue,averagePurchaseAmount"))
                .build();
    }

    // ============================================================================
    // SHARED COMPONENTS - Used by multiple steps
    // ============================================================================

    /**
     * Listener that logs progress for each chunk processed.
     * This helps us see what's happening during the batch job.
     */
    @Bean
    public ChunkLoggingListener chunkLoggingListener() {
        return new ChunkLoggingListener();
    }

    /**
     * Listener that logs job start and completion with visual formatting.
     * Makes the demo output more visually appealing.
     */
    @Bean
    public DemoJobExecutionListener jobExecutionListener() {
        return new DemoJobExecutionListener();
    }

    /**
     * Listener that logs step start and completion with visual formatting.
     * Makes the demo output more visually appealing.
     */
    @Bean
    public DemoStepExecutionListener stepExecutionListener() {
        return new DemoStepExecutionListener();
    }
}

