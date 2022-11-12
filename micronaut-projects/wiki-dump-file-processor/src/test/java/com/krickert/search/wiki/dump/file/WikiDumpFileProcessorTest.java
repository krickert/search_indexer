package com.krickert.search.wiki.dump.file;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.model.wiki.*;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
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
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:enwiki-20221101-pages-articles2.xml-short.xml.bz2");
        assertThat(resource.isPresent()).isTrue();
        String fileName = resource.get().getFile();
        Timestamp start = ProtobufUtils.now();
        Thread.sleep(200);
        DownloadedFile downloadedFile = DownloadedFile.newBuilder()
                .setDownloadStart(ProtobufUtils.now())
                .setFileDumpDate("20221101")
                .setFileName(fileName)
                .setFullFilePath(resource.get().getFile())
                .setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("65dd15906450b503691577aa6d08df2b").build())
                .setServerName("localhost")
                .setDownloadStart(start)
                .setDownloadEnd(ProtobufUtils.now())
                .build();
        this.downloadedFileProcessingProducer.sendFileProcessingRequest(downloadedFile.getFileName(), downloadedFile);
        await().atMost(100, SECONDS).until(() -> wikiArticles.size() > 100);
        await().atMost(200, SECONDS).until(() -> wikiArticles.size() >= 367);
        ProtobufUtils.saveProtocoBufsToDisk("article", wikiArticles);
    }

    @KafkaListener(
            properties =
            @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                    value = KafkaProtobufConstants.WIKIARTICLE_CLASS),
            groupId = "test-group-wiki-dump"
    )
    public static class DownloadRequestTestListener {
        @Topic("wiki-parsed-article")
        void receive(WikiArticle request,
                     long offset,
                     int partition,
                     String topic,
                     long timestamp,
                     @KafkaKey String key) {
            wikiArticles.add(request);
        }

    }
}
