package com.krickert.search.chunker.grpc;

import com.krickert.search.service.ChunkServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.grpc.server.GrpcServerChannel;

@Factory
public class Clients {

    @Bean
    ChunkServiceGrpc.ChunkServiceBlockingStub chunkBlockingStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return ChunkServiceGrpc.newBlockingStub(
                channel
        );
    }

    @Bean
    ChunkServiceGrpc.ChunkServiceStub chunkStub(
            @GrpcChannel(GrpcServerChannel.NAME)
            ManagedChannel channel) {
        return ChunkServiceGrpc.newStub(
                channel
        );
    }
}
