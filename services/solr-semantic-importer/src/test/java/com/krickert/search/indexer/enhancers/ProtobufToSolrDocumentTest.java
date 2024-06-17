package com.krickert.search.indexer.enhancers;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@MicronautTest
public class ProtobufToSolrDocumentTest {

    final ProtobufToSolrDocument unit;
    private final Collection<PipeDocument> pipeDocumentCollection;

    @Inject
    ProtobufToSolrDocumentTest(ProtobufToSolrDocument unit) {
        this.unit = unit;
        this.pipeDocumentCollection = TestDataHelper.getFewHunderedPipeDocuments();
    }

    @Test
    void testConversionOfPipeDocuments() {
        List<SolrInputDocument> solrDocuments = pipeDocumentCollection.stream()
                .map(unit::convertProtobufToSolrDocument)
                .collect(Collectors.toList());
        solrDocuments.forEach(System.out::println);
    }
}
