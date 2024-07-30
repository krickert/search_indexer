package com.krickert.search.chunker;

import io.micronaut.http.annotation.*;

@Controller("/chunker")
public class ChunkerController {

    @Get(uri = "/", produces = "text/plain")
    public String index() {
        return "Example Response";
    }
}