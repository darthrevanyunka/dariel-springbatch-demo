package com.dariel.batchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBatchDemoApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SpringBatchDemoApplication.class, args)));
    }

    /**
     * Runs both demo jobs sequentially:
     * 1. basicsJob - Simple CSV to CSV example
     * 2. customerJob - Advanced database and aggregation example
     */
    @Bean
    public CommandLineRunner runBothJobs(JobLauncher jobLauncher, 
                                         Job basicsJob, 
                                         Job customerJob) {
        return args -> {
            // Run basics job first
            System.out.println("\n" + "=".repeat(70));
            System.out.println("RUNNING BASICS JOB (Simple CSV → CSV example)");
            System.out.println("=".repeat(70) + "\n");
            
            JobParameters basicsParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(basicsJob, basicsParams);
            
            // Small delay between jobs
            Thread.sleep(500);
            
            // Run advanced job second
            System.out.println("\n" + "=".repeat(70));
            System.out.println("RUNNING ADVANCED JOB (Database & Aggregation example)");
            System.out.println("=".repeat(70) + "\n");
            
            JobParameters customerParams = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis() + 1)
                    .toJobParameters();
            jobLauncher.run(customerJob, customerParams);
        };
    }
}
