package com.krickert.search.ragsearch;

import com.krickert.search.service.EmbeddingServiceGrpc;
import io.micronaut.context.annotation.Bean;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

@Singleton
public class VectorizerContainer
        extends GenericContainer<VectorizerContainer>
        implements RagSearchServiceContainer {

    static final String DEFAULT_IMAGE = "krickert/vectorizer:latest";
    ContainerProperties vectorizerProps;

    @Inject
    public VectorizerContainer(Map<String, ContainerProperties> props) {
        super(props.get("vectorizer") == null ||
                StringUtils.isEmpty(props.get("vectorizer").getImageName()) ?
                DEFAULT_IMAGE : props.get("vectorizer").getImageName());
        this.vectorizerProps = props.get("vectorizer");
        if (vectorizerProps != null) {
            int grpcInternalPort = vectorizerProps.getPorts().getGrpc().getInternal();
            int grpcMappedPort = vectorizerProps.getPorts().getGrpc().getMapped();
            int restInternalPort = vectorizerProps.getPorts().getRest().getInternal();
            int restMappedPort = vectorizerProps.getPorts().getRest().getMapped();

            // Bind internal ports to mapped external ports
            addFixedExposedPort(grpcMappedPort, grpcInternalPort);
            addFixedExposedPort(restMappedPort, restInternalPort);
        }
    }

    @Override
    public int getGrpcPort() {
        return vectorizerProps.getPorts().getGrpc().getInternal();
    }

    @Override
    public int getRestPort() {
        return vectorizerProps.getPorts().getRest().getInternal();
    }

    @Override
    public int getMappedGrpcPort() {
        return vectorizerProps.getPorts().getGrpc().getMapped();
    }

    @Override
    public int getMappedRestPort() {
        return vectorizerProps.getPorts().getRest().getMapped();
    }
}
