package kafka.container.test;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;


import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@MicronautTest
@TestInstance(PER_CLASS)
public class KafkaContainerTestCommandTest {
    private static final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();


    @Inject
    public SampleKafkaProducer producer;

    @Test
    public void testWithCommandLineOption() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[] { "-v" };
            PicocliRunner.run(KafkaContainerTestCommand.class, ctx, args);
            producer.sendMessage("test");

            // kafka-container-test
            assertTrue(baos.toString().contains("Hi!"));
        }
    }

    @BeforeAll
    public void before() {
        messages.clear();
    }

    @Test
    public void testKafkaListens() {
        producer.sendMessage("hello");
        await().atMost(10, SECONDS).until(() -> !messages.isEmpty());
        assertEquals(1, messages.size());
        String result = messages.poll();
        assertEquals("hello", result);
    }

    @KafkaListener
    static class SampleListener {
        @Topic("sample-topic")
        public void getTopic(String message) {
            messages.add(message);
        }
    }


}
