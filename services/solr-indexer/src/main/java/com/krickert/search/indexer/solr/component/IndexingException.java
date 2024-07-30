package com.krickert.search.indexer.solr.component;

public class IndexingException extends RuntimeException {

    IndexingException(String message, Throwable t) {
        super(message, t);
    }
}
