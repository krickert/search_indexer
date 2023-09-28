package com.krickert.search.wiki.article.processor.messaging;

import io.micronaut.runtime.Micronaut;

public class WikiArticleProcessor {
    public static void main(String[] args) {
        Micronaut.run(WikiArticleProcessor.class, args);
    }
}
