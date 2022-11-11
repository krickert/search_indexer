package com.krickert.search.wiki.article.processor.messaging;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.model.wiki.*;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import jakarta.inject.Inject;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@MicronautTest
class WikiDumpFileProcessorTest {

    private static final ConcurrentLinkedQueue<WikiArticle> wikiArticles = new ConcurrentLinkedQueue<>();

    @BeforeEach
    void clear() {
        wikiArticles.clear();
    }

    @Inject
    DownloadedFileProcessingProducer downloadedFileProcessingProducer;

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    @Test
    void testProcessSampleWikiArticles() throws InterruptedException {


    }

    @KafkaListener(
            properties =
            @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                    value = KafkaProtobufConstants.WIKIARTICLE_CLASS),
            groupId = "test-wiki-article-processor-listener"
    )
    public static class DownloadRequestTestListener {
        @Topic("wiki-parsed-article")
        void receive(WikiArticle request,
                     long offset,
                     int partition,
                     String topic,
                     long timestamp) {
            wikiArticles.add(request);
        }

    }
}
