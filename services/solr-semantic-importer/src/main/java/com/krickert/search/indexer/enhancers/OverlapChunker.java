package com.krickert.search.indexer.enhancers;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The OverlapChunker class provides methods to chunk text into smaller parts with overlapping sections.
 *
 * Usage:
 * 1. Initialize an instance of the class, e.g. OverlapChunker chunker = new OverlapChunker();
 * 2. Call the chunkTextSplitNewline() method to chunk the text with overlapping sections.
 *     - This method splits the text by newline characters, squishes the lines, and then chunks each line individually.
 *     - It returns a list of the resulting chunks.
 * 3. Optionally, call the chunkText() method directly to chunk a single line of text with overlapping sections.
 *     - This method chunks the text based on the specified chunk size and overlap size.
 *     - It returns a list of the resulting chunks.
 * 4. Optionally, call the squishText() method to squish the text by removing extra whitespace and empty lines.
 *     - This method splits the text by newline characters, trims each line, and filters out empty lines.
 *     - It returns a list of the resulting non-empty lines.
 *
 * Example Usage:
 * ```java
 * OverlapChunker chunker = new OverlapChunker();
 * List<String> chunks = chunker.chunkTextSplitNewline(text, 300, 30);
 * chunks.forEach(System.out::println);
 * ```
 */
@Singleton
public class OverlapChunker implements Chunker {



    /**
     * Splits the given text into chunks, where each chunk is separated by newlines.
     * Each chunk is then further divided into smaller chunks based on the chunk size and overlap size.
     * The resulting chunks are returned as a list of strings.
     *
     * @param text         The input text to be chunked
     * @param chunkSize    The maximum size of each chunk
     * @param overlapSize  The size of the overlap between adjacent chunks
     * @return A list of strings representing the resulting chunks
     */
    public List<String> chunkTextSplitNewline(String text, int chunkSize, int overlapSize) {
        List<String> inputs = squishText(text);
        List<String> result = new ArrayList<>(inputs.size() * 2);
        for (String line : inputs) {
            result.addAll(chunkText(line, chunkSize, overlapSize));
        }
        return result;
    }
    /**
     * Splits the given text into chunks, where each chunk is separated based on the specified chunk size and overlap size.
     * The resulting chunks are returned as a list of strings.
     *
     * @param text         The input text to be chunked
     * @param chunkSize    The maximum size of each chunk
     * @param overlapSize  The size of the overlap between adjacent chunks
     * @return A list of strings representing the resulting chunks
     */
    public List<String> chunkText(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        int index = 0;

        while (index < text.length()) {
            // Calculate end of the chunk
            int end = Math.min(index + chunkSize, text.length());

            // Increase 'end' while it's not at end of text and the previous character is not a space
            while (end != text.length() && !Character.isSpaceChar(text.charAt(end))) {
                end++;
            }

            chunks.add(text.substring(index, end));

            if (end == text.length()) {
                break;
            }

            int nextIndex = Math.max(end - overlapSize, 0);
            // Decrease 'nextIndex' while it's not at start of text and the previous character is not a space
            while (nextIndex != 0 && !Character.isSpaceChar(text.charAt(nextIndex - 1))) {
                nextIndex--;
            }

            index = nextIndex;
        }
        return chunks;
    }

    /**
     * Splits the given text into lines, trims each line, and collects non-empty lines into a list.
     *
     * @param text The input text to be squished
     * @return A list of strings representing the squished lines
     */
    public List<String> squishText(String text) {
        // split by one or more newline characters
        String[] lines = text.split("\n+");

        // trim each line, and collect non-empty lines into a list
        return Arrays.stream(lines)
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> chunk(String text) {
        return chunkText(text, 300, 30);
    }
}