package com.krickert.search.indexer.enhancers;

import com.krickert.search.indexer.FileLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@MicronautTest
public class CoreNLPChunkerTest {

    @Inject
    FileLoader fileLoader;
    @Inject
    CoreNlpChunkerExample coreNlpChunkerExample;

    @Test
    void testCoreNLPChunker() throws IOException {
        String text = fileLoader.loadResource("state-of-the-union.txt");
        System.out.println(coreNlpChunkerExample.chunker(text));
    }


}
