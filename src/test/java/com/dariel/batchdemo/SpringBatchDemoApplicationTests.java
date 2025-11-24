package com.dariel.batchdemo;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify our batch job works correctly.
 */
@SpringBatchTest
@SpringBootTest
class SpringBatchDemoApplicationTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void customerJob_processesCsvAndWritesToDatabase() throws Exception {
        // Launch the batch job
        JobExecution execution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters()
        );

        // Verify job completed successfully
        assertThat(execution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Verify data was written to database
        // We have 15 rows in CSV, but 2 are invalid (row 7 has bad email, row 9 has negative amount)
        // So we should have 13 valid customers in the database
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customers", Integer.class);
        assertThat(count).isEqualTo(13);

        // Verify some data was processed correctly
        // Names should be capitalized, country should be uppercase
        String firstName = jdbcTemplate.queryForObject(
                "SELECT first_name FROM customers WHERE id = 1", String.class);
        assertThat(firstName).isEqualTo("Alex"); // Should be capitalized

        String country = jdbcTemplate.queryForObject(
                "SELECT country FROM customers WHERE id = 1", String.class);
        assertThat(country).isEqualTo("SOUTH AFRICA"); // Should be uppercase
    }
}
