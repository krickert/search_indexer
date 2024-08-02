package com.krickert.search.parser;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Section (
     String type, String body, int headingIndentation){}
