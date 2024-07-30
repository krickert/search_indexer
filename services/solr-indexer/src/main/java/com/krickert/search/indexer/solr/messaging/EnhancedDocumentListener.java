package com.krickert.search.indexer.solr.messaging;

import com.krickert.search.indexer.solr.component.SolrHelper;
import com.krickert.search.indexer.solr.component.SolrIndexingService;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.pipe.PipeDocument;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@KafkaListener(
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.PIPE_DOCUMENT_CLASS),
        offsetReset = OffsetReset.EARLIEST, threads = 5, batch = true)
@Singleton
public class EnhancedDocumentListener {
    private static final Logger log = LoggerFactory.getLogger(EnhancedDocumentListener.class);
    private final SolrIndexingService solrIndexingService;


    public EnhancedDocumentListener(SolrIndexingService solrIndexingService,
                                    SolrHelper solrHelper,
                                    @Value("${solr.create-config}") boolean createConfig,
                                    @Value("${solr.index-docs}") boolean indexDocs,
                                    @Value("${solr.reset-index}") boolean reset)
            throws SolrServerException, IOException, InterruptedException, KeeperException {
        this.solrIndexingService = solrIndexingService;
        if (reset) {
            solrHelper.reset();
        }
        if (createConfig) {
            solrHelper.uploadConfig();
        }
        if (!indexDocs) {
            System.exit(0);
        }
    }

    @Topic("enhanced-document")
    public void processEnhancedDocument(List<PipeDocument> document) {
        log.info("Got {} pipe documents to turn into enhanced documents", document.size());
        solrIndexingService.addDocuments(document);
        log.info("Finished processing enhanced documents of size {}", document.size());
    }


}
