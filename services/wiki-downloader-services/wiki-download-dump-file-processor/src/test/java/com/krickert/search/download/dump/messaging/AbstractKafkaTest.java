package com.krickert.search.download.dump.messaging;

import io.micronaut.test.support.TestPropertyProvider;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

abstract class AbstractKafkaTest implements TestPropertyProvider {

    static final KafkaContainer MY_KAFKA;

    static {
        MY_KAFKA = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:latest"));
        MY_KAFKA.start();
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap(
                "kafka.bootstrap.servers", MY_KAFKA.getBootstrapServers()
        );
    }
}