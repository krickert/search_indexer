package com.krickert.search.chunker;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TextRequestBody(String text, Integer chunkSize, Integer overlapSize) {}