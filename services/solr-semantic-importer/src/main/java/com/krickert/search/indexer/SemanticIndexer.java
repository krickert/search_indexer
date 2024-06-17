package com.krickert.search.indexer;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Singleton
public class SemanticIndexer {
    private static final Logger log = LoggerFactory.getLogger(SemanticIndexer.class);

    private final HttpSolrSelectClient httpSolrSelectClient;
    private final JsonToSolrDoc jsonToSolrDoc;
    private final String solrDestinationUrl;
    private final String solrDestinationCollection;

    @Inject
    public SemanticIndexer(HttpSolrSelectClient httpSolrSelectClient, JsonToSolrDoc jsonToSolrDoc,
                           @Value("${indexer.solr_destination_url}") String solrDestinationUrl,
                           @Value("${indexer.solr_destination_collection}") String solrDestinationCollection) {
        this.httpSolrSelectClient = httpSolrSelectClient;
        this.jsonToSolrDoc = jsonToSolrDoc;
        this.solrDestinationUrl = solrDestinationUrl;
        this.solrDestinationCollection = solrDestinationCollection;
    }

    public void indexSolrDocs(Integer paginationSize) throws IOException, InterruptedException {
        SolrClient solrClient = createSolr9Client();
        if (paginationSize == null || paginationSize <= 0) {
            throw new IllegalArgumentException("paginationSize must be greater than 0");
        }
        Collection<SolrInputDocument> docs = new ArrayList<>();
        boolean notFinished = false;
        int currentPage = 0;
        long totalExpected = -1;
        long numOfPagesExpected = -1;
        while (numOfPagesExpected != currentPage) {
            String solrDocs = httpSolrSelectClient.getSolrDocs(paginationSize, currentPage++);
            HttpSolrSelectResponse response = jsonToSolrDoc.parseSolrDocuments(solrDocs);
            if (totalExpected == -1) {
                totalExpected = response.getNumFound();
                numOfPagesExpected = (response.getNumFound() / paginationSize) + 1;
            }

            try {
                solrClient.add(response.getDocs());
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            solrClient.commit(solrDestinationCollection);
        } catch (SolrServerException e) {
            throw new RuntimeException(e);
        }
    }

    private SolrClient createSolr9Client() {
        log.info("Base Solr URL: {}", solrDestinationUrl);
        return new Http2SolrClient.Builder(solrDestinationUrl).withDefaultCollection(solrDestinationCollection).build();
    }
}
