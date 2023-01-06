package com.krickert.search.indexer.pipe.deeplearning;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import jakarta.inject.Singleton;


import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class DeepLearningServiceImpl implements DeepLearningService {


    @Override
    public PipeReply addDenseVectorFromTextService(PipeRequest request) {
        checkNotNull(request);
        PipeDocument origDoc = request.getDocument();
        PipeDocument documentReply =
                PipeDocument.newBuilder(origDoc)
                        .build();
        return PipeReply.newBuilder()
                .setDocument(documentReply)
                .setMessage("added dense vector graph")
                .build();
    }
}
