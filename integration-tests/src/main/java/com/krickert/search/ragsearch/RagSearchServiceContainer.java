package com.krickert.search.ragsearch;

public interface RagSearchServiceContainer {
    int getGrpcPort();

    int getRestPort();

    int getMappedGrpcPort();

    int getMappedRestPort();
}
