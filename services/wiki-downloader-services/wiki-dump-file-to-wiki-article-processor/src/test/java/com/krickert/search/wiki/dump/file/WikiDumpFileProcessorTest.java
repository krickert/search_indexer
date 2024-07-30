package com.krickert.search.wiki.dump.file;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import com.krickert.search.model.wiki.WikiArticle;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
class WikiDumpFileProcessorTest {

    private static final Logger log = LoggerFactory.getLogger(WikiDumpFileProcessorTest.class);

    private static final ConcurrentLinkedQueue<WikiArticle> wikiArticles = new ConcurrentLinkedQueue<>();
    @Inject
    DownloadedFileProcessingProducer downloadedFileProcessingProducer;
    @Inject
    EmbeddedApplication<?> application;
    @Value("${wiki.article.create_dummy_data}")
    Boolean createDummyData;


    @BeforeEach
    void clear() {
        wikiArticles.clear();
    }

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

    void testProcessSampleWikiArticles() throws InterruptedException {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:enwiki-20221101-pages-articles2.xml-short.xml.bz2");
        assertThat(resource.isPresent()).isTrue();
        String fileName = resource.get().getFile();
        Timestamp start = ProtobufUtils.now();
        Thread.sleep(200);
        DownloadedFile downloadedFile = DownloadedFile.newBuilder().setDownloadStart(ProtobufUtils.now()).setFileDumpDate("20221101").setFileName(fileName).setFullFilePath(resource.get().getFile()).setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("65dd15906450b503691577aa6d08df2b").build()).setServerName("localhost").setDownloadStart(start).setDownloadEnd(ProtobufUtils.now()).build();
        this.downloadedFileProcessingProducer.sendFileProcessingRequest(createKey(downloadedFile.getFileName()), downloadedFile);
        await().atMost(100, SECONDS).until(() -> wikiArticles.size() > 100);
        await().atMost(100, SECONDS).until(() -> wikiArticles.size() == 367);

        if (createDummyData) {
            ProtobufUtils.saveProtocoBufsToDisk("article", wikiArticles, 3);
        }
    }

    @KafkaListener(properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY, value = KafkaProtobufConstants.WIKIARTICLE_CLASS), groupId = "test-group-wiki-dump")
    public static class DownloadRequestTestListener {
        @Topic("wiki-parsed-article")
        void receive(WikiArticle request, String topic, @KafkaKey String key) {
            log.info("Received WikiArticle: {}: {}", wikiArticles.size() + 1, request.getTitle());
            assertNotNull(topic);
            assertNotNull(key);
            wikiArticles.add(request);
        }

    }
}