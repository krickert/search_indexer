package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

@Factory
class Clients {
    @Bean
    PipeServiceGrpc.PipeServiceBlockingStub blockingStub(
        @GrpcChannel(GrpcServerChannel.NAME) ManagedChannel channel) {
        return PipeServiceGrpc.newBlockingStub(
            channel
        );
    }
}