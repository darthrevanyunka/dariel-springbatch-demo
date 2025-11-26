package com.dariel.batchdemo.basics.config;

import com.dariel.batchdemo.basics.domain.Person;
import com.dariel.batchdemo.basics.processing.PersonProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

/**
 * BasicsJobConfig - The SIMPLEST possible Spring Batch example.
 * 
 * This demonstrates the core Spring Batch pattern:
 * READ → PROCESS (optional) → WRITE
 * 
 * Flow:
 * 1. READ: Read Person objects from input.csv
 * 2. PROCESS: Transform names to uppercase (optional - can be skipped)
 * 3. WRITE: Write Person objects to output.csv
 * 
 * This is the absolute minimum you need to understand Spring Batch!
 */
@Configuration
public class BasicsJobConfig {

    // ============================================================================
    // JOB DEFINITION
    // ============================================================================

    /**
     * Defines the basics job - the simplest possible Spring Batch job.
     * 
     * This job has ONE step that:
     * - Reads from CSV
     * - Processes (transforms) the data
     * - Writes to CSV
     */
    @Bean
    public Job basicsJob(JobRepository jobRepository, Step basicsStep) {
        return new JobBuilder("basicsJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Allows running multiple times
                .start(basicsStep)
                .build();
    }

    // ============================================================================
    // STEP: READ → PROCESS → WRITE
    // ============================================================================

    /**
     * STEP: The core Spring Batch pattern.
     * 
     * This step demonstrates:
     * - READ: Read Person objects from CSV file
     * - PROCESS: Transform data (uppercase names) - OPTIONAL
     * - WRITE: Write Person objects to CSV file
     * 
     * Items are processed in chunks of 10 for efficiency.
     */
    @Bean
    public Step basicsStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          FlatFileItemReader<Person> personReader,
                          PersonProcessor personProcessor,
                          FlatFileItemWriter<Person> personWriter) {
        return new StepBuilder("basicsStep", jobRepository)
                .<Person, Person>chunk(10, transactionManager) // Process 10 items at a time
                .reader(personReader)      // READ: Read from input.csv
                .processor(personProcessor) // PROCESS: Transform data (optional!)
                .writer(personWriter)       // WRITE: Write to output.csv
                .build();
    }

    // ============================================================================
    // READER: Reads data from source
    // ============================================================================

    /**
     * READER: Reads Person objects from a CSV file.
     * 
     * This is the first step in Spring Batch - reading data from a source.
     * 
     * Spring Batch will:
     * - Read the CSV file line by line
     * - Skip the header row
     * - Map each row to a Person object
     * - Pass each Person to the processor
     */
    @Bean
    public FlatFileItemReader<Person> personReader(
            @Value("classpath:basics/input.csv") Resource inputFile) {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personReader")
                .resource(inputFile) // The CSV file to read
                .linesToSkip(1) // Skip header row
                .delimited() // CSV format (comma-separated)
                .delimiter(DelimitedLineTokenizer.DELIMITER_COMMA)
                .names("firstName", "lastName") // CSV column names
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class); // Map to Person object
                }})
                .build();
    }

    // ============================================================================
    // PROCESSOR: Transforms/validates data (OPTIONAL!)
    // ============================================================================

    /**
     * PROCESSOR: Transforms each Person object.
     * 
     * IMPORTANT: Processor is OPTIONAL!
     * - You can have Reader → Writer without a processor
     * - If processor returns null, the item is skipped
     * - If processor returns a Person, it will be written
     * 
     * In this example, we uppercase the names.
     */
    @Bean
    public PersonProcessor personProcessor() {
        return new PersonProcessor();
    }

    // ============================================================================
    // WRITER: Writes data to destination
    // ============================================================================

    /**
     * WRITER: Writes Person objects to a CSV file.
     * 
     * This is the final step in Spring Batch - writing data to a destination.
     * 
     * Spring Batch will:
     * - Collect processed Person objects in chunks (10 at a time)
     * - Write all 10 to the CSV file at once (efficient!)
     * - This is much faster than writing one at a time
     */
    @Bean
    public FlatFileItemWriter<Person> personWriter(
            @Value("file:basics-output.csv") WritableResource outputFile) {
        return new FlatFileItemWriterBuilder<Person>()
                .name("personWriter")
                .resource(outputFile)
                .delimited()
                .delimiter(",")
                .names(new String[]{"firstName", "lastName"})
                .headerCallback(writer -> writer.write("firstName,lastName"))
                .build();
    }
}

