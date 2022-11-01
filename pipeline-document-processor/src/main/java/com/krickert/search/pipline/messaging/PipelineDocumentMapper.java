package com.krickert.search.pipline.messaging;

import com.krickert.search.model.PipelineDocument;
import org.apache.avro.specific.SpecificRecord;


public interface PipelineDocumentMapper<T extends SpecificRecord> {
    PipelineDocument mapDocument(T inputDoc);
}
