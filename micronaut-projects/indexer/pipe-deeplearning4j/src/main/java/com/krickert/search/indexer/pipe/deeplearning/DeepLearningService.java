package com.krickert.search.indexer.pipe.deeplearning;

import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;

public interface DeepLearningService {
    PipeReply addDenseVectorFromTextService(PipeRequest request);
}
