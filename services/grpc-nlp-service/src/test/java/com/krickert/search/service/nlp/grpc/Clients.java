package com.krickert.search.service.nlp.grpc;

import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

@Factory
public class Clients {

    @Bean
    PipeServiceGrpc.PipeServiceBlockingStub blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return PipeServiceGrpc.newBlockingStub(
                channel
        );
    }

    @Bean
    PipeServiceGrpc.PipeServiceStub embeddingsStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return PipeServiceGrpc.newStub(
                channel
        );
    }
}
