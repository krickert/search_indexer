package com.krickert.search.indexer.solr;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
@Serdeable
public record SearchResults (
  @JsonProperty("query") String query,
  @JsonProperty("totalResults") long totalResults,
  @JsonProperty("qTime") long qTime,
  @JsonProperty("results") List<SearchResult> results
) {}