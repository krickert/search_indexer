package com.krickert.search.vectorizer;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Singleton
public class ClientContainer {
    private static final Logger log = LoggerFactory.getLogger(ClientContainer.class);
    private final String dockerImageName;
    private final Integer grpcTestPort;
    private final Integer restTestPort;

    public String getDockerImageName() {
        return dockerImageName;
    }
    public ClientContainer(@Value("${vectorizer-test.docker.image.name}") String dockerImageName,
                           @Value("${vectorizer-test.grpc-test-port}") Integer grpcTestPort,
                           @Value("${vectorizer-test.rest-test-port}") Integer restTestPort) {
        this.dockerImageName = dockerImageName;
        this.grpcTestPort = grpcTestPort;
        this.restTestPort = restTestPort;

        DockerImageName imageName = DockerImageName.parse(dockerImageName);
        try (GenericContainer<?> container = new GenericContainer<>(imageName)
                .withExposedPorts(50401, 60401)
                .withCreateContainerCmdModifier(this::configureContainer)) {
            container.start();

            Integer port1 = container.getMappedPort(50401);
            Integer port2 = container.getMappedPort(60401);

            // Verify that the ports are mapped and the container is running
            assertTrue(container.isRunning());
            assertTrue(port1 > 0);
            assertTrue(port2 > 0);

            System.out.println("Port1: " + port1);
            System.out.println("Port2: " + port2);
        } catch (Exception e) {
            log.error("starting of the container failed with attempt for image: [{}] with rest port 50401 mapped to {} and 60401 mapped to {} threw exception {}", dockerImageName, restTestPort, grpcTestPort, e.getMessage());
            fail(e);
        }
    }
    private void configureContainer(CreateContainerCmd cmd) {
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(grpcTestPort), new ExposedPort(50401)),
                        new PortBinding(Ports.Binding.bindPort(restTestPort), new ExposedPort(60401))
                );
        cmd.withHostConfig(hostConfig);
    }
}
