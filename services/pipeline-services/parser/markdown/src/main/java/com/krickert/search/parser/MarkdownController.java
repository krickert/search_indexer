package com.krickert.search.parser;

import io.micronaut.http.annotation.*;

@Controller("/markdown")
public class MarkdownController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}