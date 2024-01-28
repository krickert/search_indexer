package com.krickert.search.pipeline.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@EachProperty("pipeline-config.client-configs")
public class PipelineClientConfig {
    private String name;
    private GRPC_TYPE type;
    private String consulService;
    private String host;
    private Integer port;
    public PipelineClientConfig(@Parameter String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GRPC_TYPE getType() {
        return type;
    }

    public void setType(GRPC_TYPE type) {
        this.type = type;
    }

    public String getConsulService() {
        return consulService;
    }

    public void setConsulService(String consulService) {
        this.consulService = consulService;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public enum GRPC_TYPE {
        CONSUL_GRPC, SERVER_GRPC
    }
}
