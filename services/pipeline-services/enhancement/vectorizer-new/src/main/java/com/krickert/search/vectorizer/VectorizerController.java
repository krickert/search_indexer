package com.krickert.search.vectorizer;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/vectorizer")
public class VectorizerController {

    @Inject
    private Vectorizer vectorizer;

    @Get(value="/embeddings", produces = MediaType.APPLICATION_JSON)
    public Collection<Float> getEmbeddings(@QueryValue String text){
        return vectorizer.getEmbeddings(text);
    }

    @Post(value="/embeddings", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Collection<Float> postEmbeddings(@Body StringBodyRequest requestBody){
        return vectorizer.getEmbeddings(requestBody.text());
    }
    
    @Get(value="/multi-embeddings", produces = MediaType.APPLICATION_JSON)
    public Collection<Collection<Float>> getMultiEmbeddings(@QueryValue List<String> texts){
        return texts.stream()
                .map(vectorizer::getEmbeddings)
                .collect(Collectors.toList());
    }

    @Post(value="/multi-embeddings", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Collection<Collection<Float>> postMultiEmbeddings(@Body ListStringBodyRequest requestBody){
        return requestBody.texts().stream()
                .map(vectorizer::getEmbeddings)
                .collect(Collectors.toList());
    }

}