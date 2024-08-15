package com.krickert.search.indexer.config;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Introspected;

@EachProperty("vector-config")
@Introspected
public class VectorConfig {

    private int chunkOverlap;
    private int chunkSize;
    private String model;
    private int dimensions;
    private String destinationCollection;
    private boolean destinationCollectionCreate;
    private String destinationCollectionVectorFieldName;

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public String getDestinationCollection() {
        return destinationCollection;
    }

    public void setDestinationCollection(String destinationCollection) {
        this.destinationCollection = destinationCollection;
    }

    public boolean isDestinationCollectionCreate() {
        return destinationCollectionCreate;
    }

    public void setDestinationCollectionCreate(boolean destinationCollectionCreate) {
        this.destinationCollectionCreate = destinationCollectionCreate;
    }

    public String getDestinationCollectionVectorFieldName() {
        return destinationCollectionVectorFieldName;
    }

    public void setDestinationCollectionVectorFieldName(String destinationCollectionVectorFieldName) {
        this.destinationCollectionVectorFieldName = destinationCollectionVectorFieldName;
    }
}