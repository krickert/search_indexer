package com.krickert.search;

import com.krickert.search.pipeline.ConsulClientFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
public class MicronautConsulServiceTest {


    @Inject
    ConsulClientFactory consulClientFactory;

    @Test
    void testGrpcClientConfig() {
        assertNotNull(consulClientFactory);
    }

    @Test
    void testWhatGrpcNameResolverReallyDoes() {
        consulClientFactory.getPipeServiceStubByName("nlp");
    }

}
