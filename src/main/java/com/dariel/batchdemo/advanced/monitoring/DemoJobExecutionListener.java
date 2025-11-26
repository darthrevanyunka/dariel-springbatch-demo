package com.dariel.batchdemo.advanced.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * Listener that logs job start and completion with visual formatting.
 * Makes the demo output more visually appealing and informative.
 */
public class DemoJobExecutionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DemoJobExecutionListener.class);

    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("🚀 BATCH JOB STARTING");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.printf("   Job: %s%n", jobExecution.getJobInstance().getJobName());
        System.out.printf("   Execution ID: %s%n", jobExecution.getId());
        System.out.println();
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        System.out.println();
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println("✅ BATCH JOB COMPLETED");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        
        if (jobExecution.getEndTime() != null && jobExecution.getStartTime() != null) {
            long duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
            System.out.printf("   Duration: %d ms%n", duration);
        }
        
        System.out.println();
        System.out.println("📊 SUMMARY STATISTICS");
        System.out.println("───────────────────────────────────────────────────────────────────");
        System.out.printf("   Total Steps: %d%n%n", jobExecution.getStepExecutions().size());
        
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            System.out.printf("   📋 Step: %s%n", stepExecution.getStepName());
            System.out.printf("      • Read:     %,10d items%n", stepExecution.getReadCount());
            System.out.printf("      • Written:  %,10d items%n", stepExecution.getWriteCount());
            System.out.printf("      • Skipped:  %,10d items%n", stepExecution.getSkipCount());
            System.out.printf("      • Filtered: %,10d items%n", stepExecution.getFilterCount());
            if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
                long stepDuration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis();
                System.out.printf("      • Duration: %,10d ms%n", stepDuration);
            }
            System.out.println();
        });
        
        System.out.println("═══════════════════════════════════════════════════════════════════");
        System.out.println();
    }
}

