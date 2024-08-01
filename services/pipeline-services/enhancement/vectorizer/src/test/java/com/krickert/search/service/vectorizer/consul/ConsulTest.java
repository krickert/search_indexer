package com.krickert.search.service.vectorizer.consul;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@MicronautTest
public class ConsulTest {

    @Container
    private static final ConsulContainer consul = new ConsulContainer(DockerImageName.parse("hashicorp/consul:latest"));

    private ApplicationContext context;

    @BeforeEach
    @Property(name = "")
    public void setup() {
        String host = consul.getHost();
        Integer port = consul.getMappedPort(8500);

        Map<String, Object> properties = new HashMap<>();
        properties.put("consul.client.registration.enabled", true);
        properties.put("consul.client.defaultZone", host + ":" + port);
        
        context = ApplicationContext.run(properties);
    }

    @Test
    public void testConsulContainer() {
        assertTrue(consul.isRunning());
        //Perform your actual test logic...
    }

    @AfterEach
    public void cleanup() {
        if (context != null) {
            context.close();
        }
    }

}