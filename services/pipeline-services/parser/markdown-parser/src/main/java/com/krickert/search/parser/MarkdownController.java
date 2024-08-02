package com.krickert.search.parser;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.serde.annotation.Serdeable;

import java.io.IOException;
import java.util.*;

@Controller("/parse")
public class MarkdownController {

    private final MarkdownService markdownService;

    public MarkdownController(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }


    @Post(value = "/markdown-file", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.APPLICATION_JSON)
        @ExecuteOn(TaskExecutors.IO)
        public HttpResponse<List<Section>> markdownFile(StreamingFileUpload file) {

        final List<Section> sections;
        try {
            sections = markdownService.parseMarkdown(file.asInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return HttpResponse.ok(sections);  // Response will be serialized to JSON automatically

        }


    @Serdeable
    public record StringBodyRequst(String markdown) {}
}