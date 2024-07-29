package com.krickert.search.chunker;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

@OpenAPIDefinition(
        info = @Info(
                title = "chunker",
                version = "0.0"
        )
)
public class ChunkerMain {

    public static void main(String[] args) {
        Micronaut.run(ChunkerMain.class, args);
    }
}