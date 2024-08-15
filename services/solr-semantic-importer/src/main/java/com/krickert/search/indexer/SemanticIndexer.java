package com.krickert.search.indexer;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.krickert.search.indexer.enhancers.ProtobufToSolrDocument;
import com.krickert.search.indexer.solr.HttpSolrSelectClient;
import com.krickert.search.indexer.solr.HttpSolrSelectResponse;
import com.krickert.search.indexer.solr.JsonToSolrDoc;
import com.krickert.search.model.pipe.PipeDocument;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class SemanticIndexer {
    private static final Logger log = LoggerFactory.getLogger(SemanticIndexer.class);

    private final ProtobufToSolrDocument protobufToSolrDocument;
    private final HttpSolrSelectClient httpSolrSelectClient;
    private final JsonToSolrDoc jsonToSolrDoc;
    private final String solrDestinationUrl;
    private final String solrDestinationCollection;

    @Inject
    public SemanticIndexer(ProtobufToSolrDocument protobufToSolrDocument, HttpSolrSelectClient httpSolrSelectClient, JsonToSolrDoc jsonToSolrDoc,
                           @Value("${indexer.destination.solr-connection.url}") String solrDestinationUrl,
                           @Value("${indexer.destination.solr-collection}") String solrDestinationCollection) {
        this.protobufToSolrDocument = protobufToSolrDocument;
        this.httpSolrSelectClient = httpSolrSelectClient;
        this.jsonToSolrDoc = jsonToSolrDoc;
        this.solrDestinationUrl = solrDestinationUrl;
        this.solrDestinationCollection = solrDestinationCollection;
    }

    public List<Message> convertDescriptorsToMessages(Collection<PipeDocument> descriptors) {
        List<Message> messages = new ArrayList<>();

        for (PipeDocument descriptor : descriptors) {
            DynamicMessage message = DynamicMessage.newBuilder(descriptor).build();
            messages.add(message);
        }

        return messages;
    }

    public void exportProtobufToSolr(Collection<Message> protos) {
        List<SolrInputDocument> solrDocuments = protos.stream()
                .map(protobufToSolrDocument::convertProtobufToSolrDocument)
                .collect(Collectors.toList());

        try (SolrClient solrClient = createSolr9Client()) {
            try {
                solrClient.add(solrDestinationCollection, solrDocuments);
                solrClient.commit(solrDestinationCollection);
            } catch (SolrServerException | IOException e) {
                log.error("Commit solr failed for collection {}", solrDestinationCollection, e);
            }
        } catch (IOException e) {
            log.error("Couldn't insert {}", protos, e);
        }
    }

    public void exportSolrDocsFromExternalSolrCollection(Integer paginationSize) throws IOException, InterruptedException {
        SolrClient solrClient = createSolr9Client();
        if (paginationSize == null || paginationSize <= 0) {
            throw new IllegalArgumentException("paginationSize must be greater than 0");
        }
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
