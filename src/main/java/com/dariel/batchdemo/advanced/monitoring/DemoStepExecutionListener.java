package com.dariel.batchdemo.advanced.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.NonNull;

import java.time.Duration;

/**
 * Listener that logs step start and completion with visual formatting.
 * Makes the demo output more visually appealing.
 */
public class DemoStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DemoStepExecutionListener.class);

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        System.out.println();
        System.out.println("───────────────────────────────────────────────────────────────────");
        System.out.printf("▶ STEP: %s%n", stepExecution.getStepName());
        System.out.println("───────────────────────────────────────────────────────────────────");
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        System.out.println();
        System.out.println("   ✓ Step completed successfully!");
        
        if (stepExecution.getEndTime() != null && stepExecution.getStartTime() != null) {
            long duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis();
            System.out.printf("   ⏱  Duration: %,d ms%n", duration);
        }
        
        return stepExecution.getExitStatus();
    }
}

