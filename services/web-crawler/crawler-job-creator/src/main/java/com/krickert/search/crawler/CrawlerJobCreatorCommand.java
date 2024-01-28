package com.krickert.search.crawler;

import io.micronaut.configuration.picocli.PicocliRunner;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "crawler-job-creator", description = "...",
        mixinStandardHelpOptions = true)
public class CrawlerJobCreatorCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Inject
    WebCrawler crawler;

    public static void main(String[] args) {
        PicocliRunner.run(CrawlerJobCreatorCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            System.out.println("Hi!");
        }

        crawler.start("http://www.cnn.com");
    }
}
