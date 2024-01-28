package com.krickert.search.download.request;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;


@MicronautTest
public class SendFileRequestsCommandTest {
    public static final ConcurrentLinkedQueue<DownloadFileRequest> fileRequest = new ConcurrentLinkedQueue<>();
    private static final Logger log = LoggerFactory.getLogger(SendFileRequestsCommandTest.class);

    @BeforeEach
    void clearFileRequests() {
        fileRequest.clear();
    }

    @Test
    public void testWithCommandLineOptionSpecifyMd5File()  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v", "-f", "./wikiList.md5"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);

        }
        await().atMost(30, SECONDS).until(() -> fileRequest.size() == 63);
        String output = baos.toString();
        assertThat(output, containsString("Application exiting."));
        log.debug("testing! {}", fileRequest);
    }


    @Test
    public void testWithNoCommandLineOptionButPropertyForFile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);
            // SendFileRequests
            await().atMost(30, SECONDS).until(() -> fileRequest.size() == 63);
            assertTrue(baos.toString().contains("Application exiting."));
        }
    }

    @Test
    public void testWithCommandLineOptionButFileNotFound() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v", "-f", "asfasdfadfafd.nothere"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);
            //let's make sure that the queue has nothing since it was a bad file
            await().atMost(30, SECONDS).until(() -> fileRequest.size() == 63);
        }
    }

    @KafkaListener(
            properties =
            @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                    value = KafkaProtobufConstants.DOWNLOAD_FILE_REQUEST_CLASS),
            groupId = "test-download-request-listener"
    )
    public static class DownloadRequestTestListener {
        @Topic("download-request")
        void receive(@KafkaKey UUID uuid,
                     DownloadFileRequest request,
                     long offset,
                     int partition,
                     String topic,
                     long timestamp) {
            log.info("uuid {}, offset {}, partition {}, topic {}, timestamp {}",
            uuid, offset, partition, topic, timestamp);
            fileRequest.add(request);
        }

    }

}
