package com.krickert.search.wiki.article.processor.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.wiki.article.processor.component.PipelineDocumentMapper;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@MicronautTest
class WikiArticleProcessorTest {

    private static final ConcurrentLinkedQueue<PipeDocument> pipeDocuments = new ConcurrentLinkedQueue<>();

    @BeforeEach
    void clear() {
        pipeDocuments.clear();
    }

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    PipeDocumentProducer producer;

    final static PipelineDocumentMapper mapper = new PipelineDocumentMapper();

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }




    @KafkaListener(
            properties =
            @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                    value = KafkaProtobufConstants.PIPE_DOCUMENT_CLASS),
            groupId = "test-wiki-pipe-processor-listener"
    )
    public static class PipeDocumentTestListener {
        @Topic("pipe-document")
        void receive(PipeDocument request,
                     long offset,
                     int partition,
                     String topic,
                     long timestamp) {
            pipeDocuments.add(request);
        }

    }
}
