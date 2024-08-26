package com.krickert.search.vectorizer;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Testcontainers
public class LoadVectorizerFromDockerIoTest {
    private static final Logger log = LoggerFactory.getLogger(LoadVectorizerFromDockerIoTest.class);

    @Inject
    ClientContainer clientContainer;

    @Test
    public void testDockerContainer() {
        assertNotNull(clientContainer);
        log.info("Successfully loaded docker container with image name: {}", clientContainer.getDockerImageName());
    }
}