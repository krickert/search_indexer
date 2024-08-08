package com.krickert.search.download.dump.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@MicronautTest
public class DownloadDumpFileListenerTest {
    private static final Logger log = LoggerFactory.getLogger(DownloadDumpFileListenerTest.class);
    static final ConcurrentLinkedQueue<DownloadedFile> results = new ConcurrentLinkedQueue<>();

    private static DownloadFileRequest createRequest(int id) {
        return DownloadFileRequest.newBuilder()
                .setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("error" + id))
                .setFileDumpDate("20221107")
                .setUrl("http://www.example.com/blah" + id + ".txt")
                .setFileName("someFile" + id + ".txt")
                .build();
    }

    @Inject
    DownloadDumpFileListener downloadDumpFileListener;
    @Inject
    DownloadRequestProducer downloadRequestProducer;
    @Inject
    DownloadedFileTestListener downloadedFileTestListener;

    @AfterEach
    void afterEach() {
        results.clear();
    }

    @Test
    void receive() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));


        DownloadFileRequest request1 = createRequest(1);
        DownloadFileRequest request2 = createRequest(2);
        DownloadFileRequest request3 = createRequest(3);
        log.info("sending request {} ", request1);
        downloadRequestProducer.sendDownloadRequest(createKey(request1), request1);
        log.info("sending request {} ", request2);
        downloadRequestProducer.sendDownloadRequest(createKey(request2), request2);
        log.info("sending request {} ", request3);
        downloadRequestProducer.sendDownloadRequest(createKey(request3), request3);
        await().atMost(30, SECONDS).until(() -> downloadedFileTestListener.getSize() == 3);

        assertThat(baos.toString())
                .contains("******SENDING TO PROCESS: someFile1.txt")
                .contains("******SENDING TO PROCESS: someFile2.txt")
                .contains("******SENDING TO PROCESS: someFile3.txt")
                .contains("http://www.example.com/blah1.txt")
                .contains("http://www.example.com/blah2.txt")
                .contains("http://www.example.com/blah3.txt");

        results.forEach((result) -> {
            try {
                File fakeDownloaded = new File(result.getFullFilePath());
                assertThat(fakeDownloaded.exists()).isTrue();
                forceDelete(fakeDownloaded);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @KafkaClient(properties = {
            @Property(name = ProducerConfig.COMPRESSION_TYPE_CONFIG, value = "lz4"),
            @Property(name = ProducerConfig.LINGER_MS_CONFIG, value = "5"),
            @Property(name = ProducerConfig.MAX_REQUEST_SIZE_CONFIG, value = "2073741824"),
            @Property(name = ProducerConfig.BATCH_SIZE_CONFIG, value = "200000"),
            @Property(name = ProducerConfig.BUFFER_MEMORY_CONFIG, value = "322122547"),
            @Property(name = ProducerConfig.ACKS_CONFIG, value = "0")
    })
    public interface DownloadRequestProducer {
        @Topic("download-request")
        void sendDownloadRequest(@KafkaKey UUID key, DownloadFileRequest request);
    }

    @KafkaListener(
            properties =
            @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                    value = KafkaProtobufConstants.DOWNLOADED_FILE_CLASS),
            groupId = "test-group-wiki-dump-file"
    )
    public static class DownloadedFileTestListener {
        @Topic("wiki-dump-file")
        void receive(DownloadedFile result) {
            log.info("Got result {} ", result);
            results.add(result);
        }

        public Integer getSize() {
            return results.size();
        }

    }
}