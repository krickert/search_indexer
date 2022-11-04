package com.krickert.search.download.request;

import com.google.protobuf.Message;
import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SendFileRequestsCommandTest {
    private static final Logger log = LoggerFactory.getLogger(SendFileRequestsCommandTest.class);
    public static final ConcurrentLinkedQueue<DownloadFileRequest> fileRequest = new ConcurrentLinkedQueue<>();

    @BeforeAll
    static void clearFileRequests() {
        fileRequest.clear();
    }

    @Test
    public void testWithCommandLineOptionSpecifyMd5File() throws Exception {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

            try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
                String[] args = new String[]{"-v", "-f", "./wikiList.md5"};
                PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);

                // SendFileRequests

            }
        //assertTrue(baos.toString().contains("Application exiting."));
            await().atMost(30, SECONDS).until(() -> fileRequest.size() == 63);
        }


    @Test
    public void testWithNoCommandLineOptionButPropertyForFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);

            // SendFileRequests
            assertTrue(baos.toString().contains("Application exiting."));
        }
    }

    @Test
    public void testWithCommandLineOptionButFileNotFound() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v", "-f", "asfasdfadfafd.nothere"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);
            //it will never reach completion
            assertEquals("", baos.toString());
        }
    }


    @KafkaListener
    public static class DownloadRequestTestListener {

        @Topic("download-request")
        void receive(@KafkaKey UUID uuid,
                     Message request,
                     long offset,
                     int partition,
                     String topic,
                     long timestamp) {
            log.error(DownloadFileRequest.class.getName());
            fileRequest.add((DownloadFileRequest) request);
        }

    }

}
