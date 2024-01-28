package com.krickert.search.pipeline;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.pipeline.component.PipelineProcessor;
import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class MockPipelineProcessor implements PipelineProcessor {
    static AtomicInteger counter = new AtomicInteger();
    @Override
    public PipeDocument process(PipeDocument pipeDocument, String pipeline) {
        Value value = Value.newBuilder().setNumberValue(counter.incrementAndGet()).build();
        Struct.Builder customData = pipeDocument.getCustomData().toBuilder().putFields("counter" + counter.get(), value);
        return pipeDocument.toBuilder().setCustomData(customData).build();
    }
}
