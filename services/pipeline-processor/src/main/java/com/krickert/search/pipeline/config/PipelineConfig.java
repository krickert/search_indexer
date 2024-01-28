package com.krickert.search.pipeline.config;

import com.google.common.collect.Maps;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Serdeable
@ConfigurationProperties("pipeline-config")
public class PipelineConfig {


    private final Map<String, PipelineClientConfig> pipelineClientConfig;
    private final Map<String, RegisteredPipelines> registeredPipelines;

    public PipelineConfig(Collection<PipelineClientConfig> pipelineClientConfig, Collection<RegisteredPipelines> registeredPipelines) {
        checkNotNull(pipelineClientConfig);
        checkNotNull(registeredPipelines);
        this.pipelineClientConfig = Maps.uniqueIndex(pipelineClientConfig, PipelineClientConfig::getName);
        this.registeredPipelines = Maps.uniqueIndex(registeredPipelines, RegisteredPipelines::getName);
    }

    public Map<String, PipelineClientConfig> getPipelineClientConfig() {
        return pipelineClientConfig;
    }

    public Map<String, RegisteredPipelines> getRegisteredPipelines() {
        return registeredPipelines;
    }

}
