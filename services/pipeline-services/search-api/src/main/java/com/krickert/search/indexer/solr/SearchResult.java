package com.krickert.search.indexer.solr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SearchResult(@JsonProperty("id") String id,
                           @JsonProperty("title") String title,
                           @JsonProperty("url") String url,
                           @JsonProperty("paragraph") String paragraph,
                           @JsonProperty("rank") int rank) { }