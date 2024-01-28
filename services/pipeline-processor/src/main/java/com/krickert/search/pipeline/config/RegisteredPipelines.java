package com.krickert.search.pipeline.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.serde.annotation.Serdeable;

import java.util.Collection;

@Serdeable
@EachProperty("pipeline-config.pipeline-processor")
public class RegisteredPipelines {

    private String name;
    private Collection<String> services;

    public Collection<String> getServices() {
        return services;
    }

    public void setServices(Collection<String> services) {
        this.services = services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public RegisteredPipelines(@Parameter("name") String name) {
        this.name = name;
    }


}
