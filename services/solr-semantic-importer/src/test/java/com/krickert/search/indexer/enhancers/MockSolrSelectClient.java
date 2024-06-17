package com.krickert.search.indexer.enhancers;

import com.krickert.search.indexer.HttpSolrSelectClient;

import java.io.IOException;

public class MockSolrSelectClient implements HttpSolrSelectClient {
    @Override
    public String getSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException {
        return "";
    }
}
