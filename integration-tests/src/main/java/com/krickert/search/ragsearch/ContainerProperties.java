package com.krickert.search.ragsearch;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@EachProperty("container-image")
public class ContainerProperties {
    private String name;
    private String imageName;
    private Ports ports;

    public ContainerProperties(@Parameter String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Ports getPorts() {
        return ports;
    }

    public void setPorts(Ports ports) {
        this.ports = ports;
    }

    @Serdeable
    public static class Ports {
        private Port grpc;
        private Port rest;

        public Port getGrpc() {
            return grpc;
        }

        public void setGrpc(Port grpc) {
            this.grpc = grpc;
        }

        public Port getRest() {
            return rest;
        }

        public void setRest(Port rest) {
            this.rest = rest;
        }

        @Serdeable
        public static class Port {
            private int internal;
            private int mapped;

            public int getInternal() {
                return internal;
            }

            public void setInternal(int internal) {
                this.internal = internal;
            }

            public int getMapped() {
                return mapped;
            }

            public void setMapped(int mapped) {
                this.mapped = mapped;
            }
        }
    }
}