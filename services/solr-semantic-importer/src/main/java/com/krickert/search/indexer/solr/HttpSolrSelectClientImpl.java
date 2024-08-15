package com.krickert.search.indexer.solr;

import com.krickert.search.indexer.SimpleGetRequest;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class HttpSolrSelectClientImpl implements HttpSolrSelectClient {
    private final SimpleGetRequest simpleGetRequest;
    private final String solrHost;
    private final String solrCollection;

    @Inject
    public HttpSolrSelectClientImpl(SimpleGetRequest simpleGetRequest,
                                    @Value("${indexer.source.solr-connection.url}") String solrHost,
                                    @Value("${indexer.source.solr-collection}") String solrCollection) {
        this.simpleGetRequest = checkNotNull(simpleGetRequest, "get request failed to load");
        this.solrHost = checkNotNull(solrHost, "solr host is needed");
        this.solrCollection = checkNotNull(solrCollection, "solr collection is needed");
    }

    @Override
    public String getSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException {
        return simpleGetRequest.getResponseAsString(createSolrRequest(paginationSize, pageNumber));
    }

    private String createSolrRequest(Integer paginationSize, Integer pageNumber) {
        return solrHost + "/" + solrCollection + "/select?q=*:*&wt=json&start=" + pageNumber * paginationSize + "&rows=" + paginationSize;
    }
}
