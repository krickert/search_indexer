package com.krickert.search.pipeline;

import io.grpc.NameResolver;
import io.micronaut.grpc.discovery.GrpcNameResolverProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class ConsulClientFactory {
    private static final Logger log = LoggerFactory.getLogger(ConsulClientFactory.class);

    private final PipelineConfig pipelineConfig;
    private final GrpcNameResolverProvider grpcNameResolverProvider;
    @Inject
    public ConsulClientFactory(PipelineConfig pipelineConfig, GrpcNameResolverProvider grpcNameResolverProvider) {
        this.pipelineConfig = checkNotNull(pipelineConfig);
        this.grpcNameResolverProvider = grpcNameResolverProvider;
    }

    public NameResolver getPipeServiceStubByName(String serviceName) {
        URI service = URI.create(serviceName);
        NameResolver resolver = grpcNameResolverProvider.newNameResolver(service, null);
        log.info(resolver.getServiceAuthority());
        resolver.refresh();
        return resolver;
    }
}
