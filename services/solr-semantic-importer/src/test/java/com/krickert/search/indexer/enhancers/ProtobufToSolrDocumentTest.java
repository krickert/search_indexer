package com.krickert.search.indexer.enhancers;

import com.krickert.search.indexer.solr.JsonToSolrDoc;
import com.krickert.search.indexer.SemanticIndexer;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.SolrContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
public class ProtobufToSolrDocumentTest {
    private static final Logger log = LoggerFactory.getLogger(ProtobufToSolrDocumentTest.class);

    final ProtobufToSolrDocument unit;
    private final Collection<PipeDocument> pipeDocumentCollection;
    private final SolrContainer container9;
    private final String testCollection = "test_collection";
    private final SemanticIndexer semanticIndexer;
    @Inject
    ProtobufToSolrDocumentTest(ProtobufToSolrDocument unit) throws SolrServerException, IOException {
        this.unit = unit;
        this.pipeDocumentCollection = TestDataHelper.getFewHunderedPipeDocuments();
        final DockerImageName solrImage = DockerImageName.parse("solr:9.6.1");
        this.container9 = createContainer(solrImage);

        try (SolrClient solrClient = createSolr9Client()) {
            solrClient.request(CollectionAdminRequest.createCollection(testCollection, "_default", 1, 1));
        }
        String solrDestinationUrl = "http://" + container9.getHost() + ":" + container9.getSolrPort() + "/solr";
        this.semanticIndexer = new SemanticIndexer(unit, new MockSolrSelectClient(), new JsonToSolrDoc(), solrDestinationUrl, "test_collection");
    }

    @Test
    void testConversionOfPipeDocuments() {
        List<SolrInputDocument> solrDocuments = pipeDocumentCollection.stream()
                .map(unit::convertProtobufToSolrDocument)
                .toList();
        solrDocuments.forEach(System.out::println);
        assertEquals(pipeDocumentCollection.size(), solrDocuments.size());
    }

    @Test
    void testInsertProtobufToSolrDocument() {
        semanticIndexer.exportProtobufToSolr(new ArrayList<>(TestDataHelper.getFewHunderedPipeDocuments()));
        try (SolrClient solrClient = createSolr9Client()) {
            try {
                QueryResponse response = solrClient.query(testCollection, new SolrQuery("*:*"));
                assertEquals(TestDataHelper.getFewHunderedPipeDocuments().size(), response.getResults().getNumFound());
            } catch (SolrServerException | IOException e) {
                fail(e);
            }
        } catch (IOException e) {
            log.error("IOException while exporting protobuf to Solr", e);
            fail(e);
        }
    }

    private SolrClient createSolr9Client() {
        String baseSolrUrl = "http://" + container9.getHost() + ":" + container9.getSolrPort() + "/solr";
        log.info("Base Solr URL: {}", baseSolrUrl);
        return new Http2SolrClient.Builder(baseSolrUrl).build();
    }
    private SolrContainer createContainer(DockerImageName image) {
        // Create the solr container.
        SolrContainer container = new SolrContainer(image);
        // Start the container. This step might take some time...
        container.start();
        return container;
    }
}
