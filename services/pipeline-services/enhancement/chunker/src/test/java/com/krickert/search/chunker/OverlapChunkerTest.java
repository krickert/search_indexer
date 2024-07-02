package com.krickert.search.chunker;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OverlapChunkerTest {

    @Test
    public void testChunkText() {
        OverlapChunker chunker = new OverlapChunker();
        String text = "This is a text with several words for testing";
        List<String> expected = Arrays.asList("This is a text", "a text with several", "several words for", "words for testing");
        List<String> actual = chunker.chunkText(text, 14, 5);
        assertEquals(expected, actual, "Text should be split into chunks correctly");
    }

    @Test
    public void testChunkTextWithOverlap() {
        OverlapChunker chunker = new OverlapChunker();
        String text = "This is a longer text with more words for testing the overlap";
        List<String> expected = Arrays.asList("This is a longer text with","longer text with more words", "with more words for testing", "for testing the overlap");
        List<String> actual = chunker.chunkText(text, 23, 11);
        assertEquals(expected, actual, "Text should be split into chunks with overlapping correctly");
    }

    @Test
    public void testChunkTextEmptyString() {
        OverlapChunker chunker = new OverlapChunker();
        String text = "";
        List<String> expected = Arrays.asList();
        List<String> actual = chunker.chunkText(text, 14, 5);
        assertEquals(expected, actual, "Empty text should return empty list");
    }
    
    @Test
    public void testChunkTextSplitNewline() {
        OverlapChunker chunker = new OverlapChunker();
        String text = "This is the first line\nThis is the second line";
        List<String> expected = Arrays.asList("This is the first line", "This is the second", "second line");
        List<String> actual = chunker.chunkTextSplitNewline(text, 18, 5);
        assertEquals(expected, actual, "Text with newlines should be chunked correctly");
    }

    @Test
    public void testSquishText() {
        OverlapChunker chunker = new OverlapChunker();
        String text = "This is    a text\n\nwith \n  several    words\nfor testing the squash";
        List<String> expected = Arrays.asList("This is a text", "with", "several words", "for testing the squash");
        List<String> actual = chunker.squishText(text);
        assertEquals(expected, actual, "Text should be squished correctly");
    }
    
}