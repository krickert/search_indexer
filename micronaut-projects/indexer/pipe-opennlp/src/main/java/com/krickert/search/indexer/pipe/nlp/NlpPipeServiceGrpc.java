package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;


@Singleton
public class NlpPipeServiceGrpc extends PipeServiceGrpc.PipeServiceImplBase {

    private final NlpService nlpService;

    public NlpPipeServiceGrpc(NlpService nlpService) {
        super();
        this.nlpService = nlpService;
    }

    @Override
    public void send(PipeRequest request, StreamObserver<PipeReply> pipeObserver) {
        pipeObserver.onNext(nlpService.processNlpService(request));
        pipeObserver.onCompleted();
    }


}
