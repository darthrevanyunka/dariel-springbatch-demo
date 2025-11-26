package com.dariel.batchdemo.advanced.monitoring;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.lang.NonNull;

/**
 * Listener that logs chunk processing with visual formatting.
 * Makes the demo output more visually appealing and shows progress.
 */
public class ChunkLoggingListener implements ChunkListener {

    private static final String CHUNK_COUNT_KEY = "chunk-count";
    private static final String PREV_READ_COUNT_KEY = "prev-read-count";
    private static final String PREV_WRITTEN_COUNT_KEY = "prev-written-count";
    private static final String PREV_SKIPPED_COUNT_KEY = "prev-skipped-count";

    @Override
    public void beforeChunk(@NonNull ChunkContext context) {
        int chunkNumber = nextChunkCounter(context);
        System.out.printf("  📦 Processing chunk #%d...%n", chunkNumber);
    }

    @Override
    public void afterChunk(@NonNull ChunkContext context) {
        int chunkNumber = currentChunkCounter(context);
        var executionContext = context.getStepContext().getStepExecution().getExecutionContext();
        var stepExecution = context.getStepContext().getStepExecution();
        
        // Get current cumulative counts
        long currentRead = stepExecution.getReadCount();
        long currentWritten = stepExecution.getWriteCount();
        long currentSkipped = stepExecution.getSkipCount();
        
        // Get previous counts (default to 0 for first chunk)
        long prevRead = executionContext.getLong(PREV_READ_COUNT_KEY, 0);
        long prevWritten = executionContext.getLong(PREV_WRITTEN_COUNT_KEY, 0);
        long prevSkipped = executionContext.getLong(PREV_SKIPPED_COUNT_KEY, 0);
        
        // Calculate delta for this chunk
        long chunkRead = currentRead - prevRead;
        long chunkWritten = currentWritten - prevWritten;
        long chunkSkipped = currentSkipped - prevSkipped;
        
        System.out.printf("  ✓ Chunk #%d completed | Read: %d, Written: %d, Skipped: %d%n", 
                chunkNumber, chunkRead, chunkWritten, chunkSkipped);
        
        // Store current counts for next chunk
        executionContext.putLong(PREV_READ_COUNT_KEY, currentRead);
        executionContext.putLong(PREV_WRITTEN_COUNT_KEY, currentWritten);
        executionContext.putLong(PREV_SKIPPED_COUNT_KEY, currentSkipped);
    }

    @Override
    public void afterChunkError(@NonNull ChunkContext context) {
        int chunkNumber = currentChunkCounter(context);
        System.out.printf("  ✗ Chunk #%d failed, rolling back%n", chunkNumber);
    }

    private int nextChunkCounter(ChunkContext context) {
        int currentValue = currentChunkCounter(context);
        context.getStepContext().getStepExecution().getExecutionContext().putInt(CHUNK_COUNT_KEY, currentValue + 1);
        return currentValue + 1;
    }

    private int currentChunkCounter(ChunkContext context) {
        return context.getStepContext().getStepExecution()
                .getExecutionContext()
                .getInt(CHUNK_COUNT_KEY, 0);
    }
}

