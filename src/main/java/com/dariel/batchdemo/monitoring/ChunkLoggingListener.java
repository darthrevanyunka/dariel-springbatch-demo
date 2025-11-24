package com.dariel.batchdemo.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.lang.NonNull;

public class ChunkLoggingListener implements ChunkListener {

    private static final Logger log = LoggerFactory.getLogger(ChunkLoggingListener.class);
    private static final String CHUNK_COUNT_KEY = "chunk-count";

    @Override
    public void beforeChunk(@NonNull ChunkContext context) {
        int chunkNumber = nextChunkCounter(context);
        log.info("[{}] Starting chunk #{}", context.getStepContext().getStepName(), chunkNumber);
    }

    @Override
    public void afterChunk(@NonNull ChunkContext context) {
        int chunkNumber = currentChunkCounter(context);
        log.info("[{}] Completed chunk #{} | read={}, written={}, skipped={}",
                context.getStepContext().getStepName(),
                chunkNumber,
                context.getStepContext().getStepExecution().getReadCount(),
                context.getStepContext().getStepExecution().getWriteCount(),
                context.getStepContext().getStepExecution().getSkipCount());
    }

    @Override
    public void afterChunkError(@NonNull ChunkContext context) {
        int chunkNumber = currentChunkCounter(context);
        log.warn("[{}] Chunk #{} failed, rolling back", context.getStepContext().getStepName(), chunkNumber);
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

