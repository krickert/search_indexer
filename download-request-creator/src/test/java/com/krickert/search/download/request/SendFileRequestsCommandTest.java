package com.krickert.search.download.request;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class SendFileRequestsCommandTest {

    @Test
    public void testWithCommandLineOptionSpecifyMd5File() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        try (ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)) {
            String[] args = new String[]{"-v", "-f", "./wikiList.md5"};
            PicocliRunner.run(SendFileRequestsCommand.class, ctx, args);

            // SendFileRequests
            assertTrue(baos.toString().contains("Application exiting."));
        }
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

}
