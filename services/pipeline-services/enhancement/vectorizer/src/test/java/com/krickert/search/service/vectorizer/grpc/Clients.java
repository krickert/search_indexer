package com.krickert.search.service.vectorizer.grpc;

import com.krickert.search.service.EmbeddingServiceGrpc;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

@Factory
public class Clients {

    @Bean
    PipeServiceGrpc.PipeServiceBlockingStub pipeServiceBlockingStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return PipeServiceGrpc.newBlockingStub(
                channel
        );
    }

    @Bean
    PipeServiceGrpc.PipeServiceStub pipeServiceStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return PipeServiceGrpc.newStub(
                channel
        );
    }

    @Bean
    EmbeddingServiceGrpc.EmbeddingServiceBlockingStub blockingEmbeddingServiceStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return EmbeddingServiceGrpc.newBlockingStub(
                channel
        );
    }

    @Bean
    EmbeddingServiceGrpc.EmbeddingServiceStub embeddingServiceStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return EmbeddingServiceGrpc.newStub(
                channel
        );
    }
}
