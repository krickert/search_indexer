package com.krickert.search.indexer;

import jakarta.inject.Singleton;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class HttpSolrSelectClientImpl implements HttpSolrSelectClient {
    private final SimpleGetRequest simpleGetRequest;
    private final String solrHost;
    private final String solrCollection;

    public HttpSolrSelectClientImpl(SimpleGetRequest simpleGetRequest, String solrHost, String solrCollection) {
        this.simpleGetRequest = checkNotNull(simpleGetRequest, "get request failed to load");
        this.solrHost = checkNotNull(solrHost, "solr host is needed");
        this.solrCollection = checkNotNull(solrCollection, "solr collection is needed");
    }

    public String getSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException {
        return simpleGetRequest.getResponseAsString(createSolrRequest(paginationSize, pageNumber));
    }

    private String createSolrRequest(Integer paginationSize, Integer pageNumber) {
        return solrHost + "/" + solrCollection + "/select/q=*:*&start=" + (pageNumber - 1) * paginationSize + "&rows=" + paginationSize;
    }
}
