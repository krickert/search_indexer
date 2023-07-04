package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;

public interface NlpService {
    PipeReply processNlpService(PipeRequest request);
}
