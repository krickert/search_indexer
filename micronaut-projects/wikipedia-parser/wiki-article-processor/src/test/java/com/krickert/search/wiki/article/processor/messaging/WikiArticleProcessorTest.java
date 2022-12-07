package com.krickert.search.wiki.article.processor.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.model.util.ProtobufUtils;
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

import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static java.util.concurrent.TimeUnit.SECONDS;
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
    WikiArticleProcessingProducer producer;

    final static PipelineDocumentMapper mapper = new PipelineDocumentMapper();

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }


    @Test
    void testPipeDocumentProcessing() {
        TestDataHelper.getFewHunderedArticles()
                .forEach((wikiArticle) ->
                        producer.sendFileProcessingRequest(
                                createKey(wikiArticle),
                                wikiArticle));
        await().atMost(30, SECONDS).until(() -> pipeDocuments.size() > 20);
        await().atMost(60, SECONDS).until(() -> pipeDocuments.size() > 100);
        await().atMost(90, SECONDS).until(() -> pipeDocuments.size() == 367);
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
