package com.krickert.search.indexer;

import com.krickert.search.indexer.config.IndexerConfiguration;
import com.krickert.search.indexer.config.VectorConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test")
public class IndexerConfigurationTest {

    @Inject
    IndexerConfiguration config;

    @Inject
    Map<String, VectorConfig> vectorConfig;

    @BeforeAll
    static void setup() {
        ApplicationContext ctx = ApplicationContext.run();
        IndexerConfiguration config = ctx.getBean(IndexerConfiguration.class);
        System.out.println("Loaded configuration: " + config.getSource().getSolrVersion());
    }

    @Test
    void testConfigurationValues() {
        assertNotNull(config);

        // Source configuration
        assertNotNull(config.getSource());
        assertEquals("7.7.3", config.getSource().getSolrVersion());
        assertEquals("source_collection", config.getSource().getSolrCollection());
        assertNotNull(config.getSource().getSeedData());
        assertFalse(config.getSource().getSeedData().isEnabled());
        assertEquals("sample_solr_result.json", config.getSource().getSeedData().getSeedJsonFile());
        assertNotNull(config.getSource().getSolrConnection());
        assertEquals("http://localhost:8983/solr", config.getSource().getSolrConnection().getUrl());
        assertNotNull(config.getSource().getSolrConnection().getAuthentication());
        assertFalse(config.getSource().getSolrConnection().getAuthentication().isEnabled());

        // Destination configuration
        assertNotNull(config.getDestination());
        assertEquals("9.6.1", config.getDestination().getSolrVersion());
        assertEquals("destination_collection", config.getDestination().getSolrCollection());
        assertNotNull(config.getDestination().getSolrConnection());
        assertEquals("http://localhost:8983/solr", config.getDestination().getSolrConnection().getUrl());
        assertNotNull(config.getDestination().getSolrConnection().getAuthentication());
        assertTrue(config.getDestination().getSolrConnection().getAuthentication().isEnabled());
        assertEquals("jwt", config.getDestination().getSolrConnection().getAuthentication().getType());
        assertEquals("my-client-secret", config.getDestination().getSolrConnection().getAuthentication().getClientSecret());
        assertEquals("my-client-id", config.getDestination().getSolrConnection().getAuthentication().getClientId());
        assertEquals("https://my-token-url.com/oauth2/some-token/v1/token", config.getDestination().getSolrConnection().getAuthentication().getIssuer());
        assertEquals("issuer-auth-id", config.getDestination().getSolrConnection().getAuthentication().getIssuerAuthId());

    }

    @Test
    void testVectorConfig() {
        // Vector config (nested under destination)
        assertNotNull(vectorConfig);
        System.out.println("Test - Vector config size: " + vectorConfig.size());

        assertEquals(2, vectorConfig.size());

        VectorConfig vectorConfig1 = vectorConfig.get("title");
        assertNotNull(vectorConfig1);
        assertEquals(30, vectorConfig1.getChunkOverlap());
        assertEquals(300, vectorConfig1.getChunkSize());
        assertEquals("mini-LM", vectorConfig1.getModel());
        assertEquals(384, vectorConfig1.getDimensions());
        assertEquals("title_vector", vectorConfig1.getDestinationCollection());
        assertTrue(vectorConfig1.isDestinationCollectionCreate());
        assertEquals("title_mini_lm", vectorConfig1.getDestinationCollectionVectorFieldName());

        VectorConfig vectorConfig2 = vectorConfig.get("body");
        assertNotNull(vectorConfig2);
        assertEquals(30, vectorConfig2.getChunkOverlap());
        assertEquals(300, vectorConfig2.getChunkSize());
        assertEquals("mini-LM", vectorConfig2.getModel());
        assertEquals(384, vectorConfig2.getDimensions());
        assertEquals("body_vector", vectorConfig2.getDestinationCollection());
        assertTrue(vectorConfig2.isDestinationCollectionCreate());
        assertEquals("title_mini_lm", vectorConfig2.getDestinationCollectionVectorFieldName());
    }
}