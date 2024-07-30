package com.krickert.search.indexer.solr;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;

@Controller("/search-api")
public class SearchApiController {

    final BasicSearchService basicSearchService;

    public SearchApiController(BasicSearchService basicSearchService) {
        this.basicSearchService = basicSearchService;
    }

    @Get(uri="/", produces= MediaType.APPLICATION_JSON)
    public SearchResults search(@Parameter String q, @Parameter TYPE type) throws SolrServerException, IOException {
        return basicSearchService.search(q, type);
    }

    public enum TYPE {

        ARTICLE_SEMANTIC("wiki_article"),
        ARTICLE_KEYWORD("wiki_article"),
        ARTICLE_RERANK("wiki_article"),
        PARAGRAPH_SEMANTIC("wiki_paragraph"),
        PARAGRAPH_KEYWORD("wiki_paragraph"),
        PARAGRAPH_RERANK("wiki_paragraph"),
        EVERYTHING_KEYWORD("wiki");


        final String collection;

        private TYPE(String collection) {
            this.collection = collection;
        }
    }
}