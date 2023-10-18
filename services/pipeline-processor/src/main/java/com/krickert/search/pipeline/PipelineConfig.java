package com.krickert.search.pipeline;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

@Serdeable
@ConfigurationProperties("pipeline-config")
public class PipelineConfig {

    private final Collection<PipelineClientConfig> pipelineClientConfig;
    private final Collection<RegisteredPipelines> registeredPipelines;

    public PipelineConfig(Collection<PipelineClientConfig> pipelineClientConfig, Collection<RegisteredPipelines> registeredPipelines) {
        this.pipelineClientConfig = checkNotNull(pipelineClientConfig);
        this.registeredPipelines = checkNotNull(registeredPipelines);
    }

    public Collection<RegisteredPipelines> getRegisteredPipelines() {
        return registeredPipelines;
    }

    public Collection<PipelineClientConfig> getPipelineClientConfig() {
        return pipelineClientConfig;
    }
}
