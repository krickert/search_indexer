package com.krickert.search.indexer;

import io.micronaut.configuration.picocli.PicocliRunner;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(name = "solr-semantic-importer", description = "...",
        mixinStandardHelpOptions = true)
public class SolrSemanticImporterCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Option(names = {"-e", "--enabled"}, description = "skip the indexing and exit")
    boolean enabled = false;

    @Inject
    SemanticIndexer semanticIndexer;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(SolrSemanticImporterCommand.class, args);
    }

    public void run() {
        // business logic here
        if (verbose) {
            System.out.println("Hi!");
        }
        try {
            if (enabled) {
                semanticIndexer.exportSolrDocsFromExternalSolrCollection(5);
                System.out.println("Done!");
                System.exit(0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
