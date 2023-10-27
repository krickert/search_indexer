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
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;

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
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

//@MicronautTest
@TestInstance(PER_CLASS)
class DownloadDumpFileListenerTest extends AbstractKafkaTest {


    static final ConcurrentLinkedQueue<DownloadedFile> results = new ConcurrentLinkedQueue<>();
    DownloadFileRequest request1 = DownloadFileRequest.newBuilder()
            .setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("error1"))
            .setFileDumpDate("20221107")
            .setUrl("http://www.example.com/blah1.txt")
            .setFileName("someFile1.txt")
            .build();
    DownloadFileRequest request2 = DownloadFileRequest.newBuilder()
            .setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("error2"))
            .setFileDumpDate("20221107")
            .setUrl("http://www.example.com/blah2.txt")
            .setFileName("someFile2.txt")
            .build();
    DownloadFileRequest request3 = DownloadFileRequest.newBuilder()
            .setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("error3"))
            .setFileDumpDate("20221107")
            .setUrl("http://www.example.com/blah3.txt")
            .setFileName("someFile3.txt")
            .build();
    @Inject
    DownloadDumpFileListener downloadDumpFileListener;
    @Inject
    DownloadRequestProducer downloadRequestProducer;

    @AfterEach
    public void clearQueue() {
        results.clear();
    }

    //@Test
    void receive() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        downloadRequestProducer.sendDownloadRequest(createKey(request3), request1);
        downloadRequestProducer.sendDownloadRequest(createKey(request3), request2);
        downloadRequestProducer.sendDownloadRequest(createKey(request3), request3);
        await().atMost(30, SECONDS).until(() -> results.size() == 3);

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


    @KafkaClient
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
        void receive(DownloadedFile request) {
            results.add(request);
        }

    }
}