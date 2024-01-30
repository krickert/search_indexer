package com.krickert.search;

import com.krickert.search.pipeline.config.PipelineClientConfig;
import com.krickert.search.pipeline.config.PipelineConfig;
import com.krickert.search.pipeline.config.RegisteredPipelines;
import io.micronaut.context.annotation.Value;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@MicronautTest
public class PipelineClientConfigTest {

    @Value("${grpc.server.port}")
    String grpcServerPort;

    @Test
    void testPipelineConfig(PipelineConfig pipelineConfig) {
        assertNotNull(pipelineConfig);
        assertEquals(3, pipelineConfig.getPipelineClientConfig().size());
    }

    @Test
    void testPipelineClientConfiguration(@Named("nlp") PipelineClientConfig nlp,
                                         @Named("vectorizer") PipelineClientConfig vectorizer,
                                         @Named("nlp2") PipelineClientConfig nlp2) {
        assertEquals(PipelineClientConfig.GRPC_TYPE.CONSUL_GRPC, nlp.getType());
        assertEquals("nlp", nlp.getName());
        assertEquals(PipelineClientConfig.GRPC_TYPE.CONSUL_GRPC, vectorizer.getType());
        assertEquals("vectorizer", vectorizer.getName());
        assertEquals(PipelineClientConfig.GRPC_TYPE.SERVER_GRPC, nlp2.getType());
        assertEquals("nlp2", nlp2.getName());
        assertEquals(Integer.valueOf(grpcServerPort), nlp2.getPort());
        assertEquals("localhost", nlp2.getHost());
    }

    @Test
    void testServiceRegistrationConfiguration(
            @Named("datascience-pipeline") RegisteredPipelines datasciencePipeline,
            @Named("search-pipeline") RegisteredPipelines searchPipeline) {
        assertEquals("search-pipeline", searchPipeline.getName());
        assertEquals(3, searchPipeline.getServices().size());
        assertEquals("datascience-pipeline", datasciencePipeline.getName());
        assertEquals(1, datasciencePipeline.getServices().size());
    }
}
