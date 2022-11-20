package com.krickert.search;

import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;

@Singleton
public class PipeService extends PipeServiceGrpc.PipeServiceImplBase {
    @Override
    public void send(PipeRequest request, StreamObserver<PipeReply> responseObserver) {

    }

}
