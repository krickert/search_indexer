package com.krickert.search.pipline.messaging;

import com.google.protobuf.GeneratedMessageV3;
import com.krickert.search.model.pipe.PipeDocument;


public interface PipelineDocumentMapper<T extends GeneratedMessageV3> {
    PipeDocument mapDocument(T inputDoc);
}
