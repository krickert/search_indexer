package com.krickert.search.chunker;

import com.google.common.net.InternetDomainName;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class OverlapChunker {

    private static final Logger log = LoggerFactory.getLogger(OverlapChunker.class);

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
        String[] texts = text.split("\n+");
        for (String input : texts) {

        }
        text = squish(text.replaceAll("[^\\p{L}\\p{N}.' ]", " "));

        int index = 0;
        int chunkCount = 0;
        String prevChunk = null;

        while (index < text.length() && chunkCount < 2000) {
            // Calculate end of the chunk
            int end = Math.min(index + chunkSize, text.length());

            // Increase 'end' while it's not at end of text and the previous character is not a space
            while (end != text.length() && !Character.isSpaceChar(text.charAt(end))) {
                end++;
            }

            String currentChunk = text.substring(index, end).trim();

            if (currentChunk.equals(prevChunk)) {
                // If the chunk is the same as the previous one, we skip this iteration
                index = end + 1;
                continue;
            }

            chunks.add(transformURLsToWords(currentChunk));

            if (end == text.length()) {
                break;
            }

            int nextIndex = Math.max(end - overlapSize, 0);
            int previousIndex = nextIndex;  // Store the original calculated start of the next chunk
            // Modify this while loop to include spaces at beginning of the overlap
            while (nextIndex != 0 && !Character.isWhitespace(text.charAt(nextIndex - 1)) && text.charAt(nextIndex - 1) != ' ') {
                nextIndex--;
            }
            // Updated check - if nextIndex hasn't moved and the word's length is greater than overlap
            if (previousIndex == nextIndex && (end - nextIndex > overlapSize) && nextIndex != 0) {
                nextIndex = end + 1;
            }

            chunkCount++;
            index = nextIndex;
            prevChunk = currentChunk;
        }

        if (chunkCount == 2000) {
            log.warn("Over 2000 chunks in one document");
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

    public String squish(String text) {
        return text.replaceAll("\\s+", " ").trim().replaceAll("\\n+", " ");
    }


    public String transformURLsToWords(String inputText){
        // The regex to match URLs
        Pattern urlPattern = Pattern.compile("\\b(?:[a-z][a-z0-9+.-]*:)?//?([a-z0-9.-]+\\.[a-z]{2,})([\\w-./?=&#%]*)?\\b");
        Matcher matcher = urlPattern.matcher(inputText);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String validURLPart = "";

            String potentialDomain = matcher.group(1); // group(1) is now the domain part
            if (InternetDomainName.isValid(potentialDomain)) {
                // Split the domain by "." and use all parts except last one (the TLD)
                String[] domainParts = potentialDomain.split("\\.");
                String domainWithoutTld = String.join(" ", Arrays.copyOf(domainParts, domainParts.length - 1));

                // If there's a slug (group 2), split on non-word characters to make it into words, and append
                String slugAndQueryAsWords = "";
                if(matcher.group(2) != null){
                    slugAndQueryAsWords = matcher.group(2).replaceAll("[/=?&%]+", " ");
                }

                // Replace the URL with the transformed text (words)
                matcher.appendReplacement(buffer, domainWithoutTld + " " + slugAndQueryAsWords);
            } else {
                System.out.println("Invalid URL or domain: " + matcher.group());
            }
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

}