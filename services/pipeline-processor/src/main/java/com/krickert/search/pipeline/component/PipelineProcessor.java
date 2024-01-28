package com.krickert.search.pipeline.component;

import com.krickert.search.model.pipe.PipeDocument;

public interface PipelineProcessor {
    PipeDocument process(PipeDocument pipeDocument, String pipeline);
}
