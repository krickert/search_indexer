package com.krickert.search.indexer.enhancers;

import com.krickert.search.indexer.solr.HttpSolrSelectClient;

import java.io.IOException;

public class MockSolrSelectClient implements HttpSolrSelectClient {
    @Override
    public String getSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException {
        return "";
    }
}
