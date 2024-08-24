package com.krickert.search.ragsearch;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

@MicronautTest
class ContainerPropertiesTest {
    
    @Inject
    private Map<String, ContainerProperties> containerPropertiesMap;

    @Test
    void testContainerProperties() {
        Assertions.assertNotNull(containerPropertiesMap);
        Assertions.assertEquals(2, containerPropertiesMap.size());
        Assertions.assertTrue(containerPropertiesMap.containsKey("vectorizer"));
        Assertions.assertTrue(containerPropertiesMap.containsKey("chunker"));

        ContainerProperties vectorizerProperties = containerPropertiesMap.get("vectorizer");
        Assertions.assertNotNull(vectorizerProperties);
        Assertions.assertEquals("krickert/vectorizer:1.0-SNAPSHOT", vectorizerProperties.getImageName());
        Assertions.assertEquals("vectorizer", vectorizerProperties.getName());

        ContainerProperties chunkerProperties = containerPropertiesMap.get("chunker");
        Assertions.assertNotNull(chunkerProperties);
        Assertions.assertEquals("krickert/chunker:1.0-SNAPSHOT", chunkerProperties.getImageName());
        Assertions.assertEquals("chunker", chunkerProperties.getName());
    }

    @Test
    void testGetNameWhenValueIsSet() {
        String testName = "TestContainer";
        ContainerProperties.Ports ports = new ContainerProperties.Ports();
        ContainerProperties containerProperties = new ContainerProperties(testName);
        containerProperties.setPorts(ports);
        Assertions.assertEquals(testName, containerProperties.getName());
    }

    @Test
    void testGetNameWhenValueIsUpdated() {
        String initialName = "InitialContainer";
        String updatedName = "UpdatedContainer";
        ContainerProperties.Ports ports = new ContainerProperties.Ports();
        ContainerProperties containerProperties = new ContainerProperties(initialName);
        containerProperties.setPorts(ports);
        containerProperties.setName(updatedName);
        Assertions.assertEquals(updatedName, containerProperties.getName());
    }

    @Test
    void testGetNameWhenValueIsNotSet() {
        ContainerProperties.Ports ports = new ContainerProperties.Ports();
        ContainerProperties containerProperties = new ContainerProperties(null);
        containerProperties.setPorts(ports);
        Assertions.assertNull(containerProperties.getName());
    }
}