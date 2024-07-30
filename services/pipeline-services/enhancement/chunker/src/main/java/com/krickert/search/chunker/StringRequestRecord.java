package com.krickert.search.chunker;

import io.micronaut.serde.annotation.Serdeable;


@Serdeable
public record StringRequestRecord(String text) {
}
