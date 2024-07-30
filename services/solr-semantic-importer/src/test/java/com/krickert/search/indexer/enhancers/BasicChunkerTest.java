package com.krickert.search.indexer.enhancers;

import com.krickert.search.indexer.FileLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@MicronautTest
public class BasicChunkerTest {

    @Inject
    FileLoader fileLoader;
    @Inject
    OverlapChunker overlapChunker;

    @Test
    void testCoreNLPChunker() throws IOException {
        String text = fileLoader.loadResource("state-of-the-union.txt");
        overlapChunker.chunkTextSplitNewline(text, 300, 30).forEach(System.out::println);
    }


}
