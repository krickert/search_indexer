package com.krickert.search.chunker;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import java.util.List;

@Controller("/chunker")
public class OverlapChunkController {

    @Inject
    private OverlapChunker overlapChunker;

    @Get(value="/chunk-text-split-newline", produces = MediaType.APPLICATION_JSON)
    public List<String> getChunkTextSplitNewline(@QueryValue String text, @QueryValue String chunkSize, @QueryValue String overlapSize) {
        return overlapChunker.chunkTextSplitNewline(text, Integer.parseInt(chunkSize), Integer.parseInt(overlapSize));
    }

    @Post(value="/chunk-text-split-newline", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public List<String> postChunkTextSplitNewline(@Body TextRequestBody requestBody) {
        return overlapChunker.chunkTextSplitNewline(requestBody.text(), requestBody.chunkSize(), requestBody.overlapSize());
    }

    @Get(value="/chunk-text", produces = MediaType.APPLICATION_JSON)
    public List<String> getChunkText(@QueryValue String text, @QueryValue String chunkSize, @QueryValue String overlapSize) {
        return overlapChunker.chunkText(text, Integer.parseInt(chunkSize), Integer.parseInt(overlapSize));
    }

    @Post(value="/chunk-text", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public List<String> postChunkText(@Body TextRequestBody requestBody) {
        return overlapChunker.chunkText(requestBody.text(), requestBody.chunkSize(), requestBody.overlapSize());
    }   

    @Get(value="/squish-text", produces = MediaType.APPLICATION_JSON)
    public List<String> getSquishText(@QueryValue String text) {
        return overlapChunker.squishText(text);
    }  

    @Post(value="/squish-text", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public List<String> postSquishText(@Body StringRequestRecord requestBody) {
        return overlapChunker.squishText(requestBody.text());
    }   

    @Get(value="/squish", produces = MediaType.TEXT_PLAIN)
    public String getSquish(@QueryValue String text) {
        return overlapChunker.squish(text);
    }   

    @Post(value="/squish", consumes = MediaType.APPLICATION_JSON, produces = MediaType.TEXT_PLAIN)
    public String postSquish(@Body StringRequestRecord requestBody) {
        return overlapChunker.squish(requestBody.text());
    }  

    @Get(value="/transform-urls-to-words", produces = MediaType.TEXT_PLAIN)
    public String getTransformUrlsToWords(@QueryValue String text) {
        return overlapChunker.transformURLsToWords(text);
    }   

    @Post(value="/transform-urls-to-words", consumes = MediaType.APPLICATION_JSON, produces = MediaType.TEXT_PLAIN)
    public String postTransformUrlsToWords(@Body StringRequestRecord requestBody) {
        return overlapChunker.transformURLsToWords(requestBody.text());
    }
}