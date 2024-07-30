package com.krickert.search.vectorizer;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ListStringBodyRequest(List<String> texts) {
}
