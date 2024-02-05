package com.krickert.search.indexer.solr;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;

import java.util.List;
import java.util.Optional;

@Factory
public class SolrClientFactory {
    private final String solrHost;

    public SolrClientFactory(@Value("${solr.host}") String solrHost) {
        this.solrHost = solrHost;
    }

    @Singleton
    SolrClient client() {
        return new Http2SolrClient.Builder(solrHost).withBasicAuthCredentials("solr", "SolrRocks").build();
    }
}
