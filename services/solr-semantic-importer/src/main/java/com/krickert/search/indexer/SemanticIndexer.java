package com.krickert.search.indexer;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.solr.common.SolrDocument;

import java.io.IOException;
import java.util.Collection;

@Singleton
public class SemanticIndexer {

    private final HttpSolrSelectClientImpl httpSolrSelectClient;
    private final JsonToSolrDoc jsonToSolrDoc;
    @Inject
    public SemanticIndexer(HttpSolrSelectClientImpl httpSolrSelectClient, JsonToSolrDoc jsonToSolrDoc) {
        this.httpSolrSelectClient = httpSolrSelectClient;
        this.jsonToSolrDoc = jsonToSolrDoc;
    }

    public void indexSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException {
        String solrDocs = httpSolrSelectClient.getSolrDocs(paginationSize, pageNumber);
        Collection<SolrDocument> solrDocument = jsonToSolrDoc.parseSolrDocuments(solrDocs);
    }
}
