package com.krickert.search.ragsearch;

import com.krickert.search.service.EmbeddingServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Value;
import io.micronaut.grpc.annotation.GrpcChannel;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class VectorizerContainerTest {
    @Inject
    private VectorizerContainer vectorizerContainer;

    @Bean
    EmbeddingServiceGrpc.EmbeddingServiceBlockingStub blockingEmbeddingServiceStub(
            @GrpcChannel("${container-image.vectorizer.ports.grpc.mapped}")
            ManagedChannel channel) {
        return EmbeddingServiceGrpc.newBlockingStub(
                channel
        );
    }

    @Test
    void testVectorizerContainer() {
        Assertions.assertNotNull(vectorizerContainer);
        // Start the container
        vectorizerContainer.start();

        // Validate internal and mapped ports are set correctly
        assertEquals(50401, vectorizerContainer.getGrpcPort());
        assertEquals(60401, vectorizerContainer.getRestPort());
        assertEquals(vectorizerContainer.getMappedPort(50401), vectorizerContainer.getMappedGrpcPort());
        assertEquals(vectorizerContainer.getMappedPort(60401), vectorizerContainer.getMappedRestPort());
    }
}
