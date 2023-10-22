package com.krickert.search.pipeline;

import com.krickert.search.model.pipe.PipeDocument;

public interface PipelineProcessor {
    PipeDocument process(PipeDocument pipeDocument, String pipeline);
}
