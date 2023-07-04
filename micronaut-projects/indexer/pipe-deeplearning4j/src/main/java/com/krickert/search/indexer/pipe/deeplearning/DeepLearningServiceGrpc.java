package com.krickert.search.indexer.pipe.deeplearning;

import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;


@Singleton
public class DeepLearningServiceGrpc extends PipeServiceGrpc.PipeServiceImplBase {

    private final DeepLearningService deepLearningService;

    public DeepLearningServiceGrpc(DeepLearningService deepLearningService) {
        super();
        this.deepLearningService = deepLearningService;
    }

    @Override
    public void send(PipeRequest request, StreamObserver<PipeReply> pipeObserver) {
        pipeObserver.onNext(deepLearningService.addDenseVectorFromTextService(request));
        pipeObserver.onCompleted();
    }


}
