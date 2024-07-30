package com.krickert.search.wiki.dump.file.component;

import com.krickert.search.model.wiki.Link;
import com.krickert.search.wiki.dump.file.component.WikiURLExtractor;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * This class includes tests for the WikiURLExtractor.
 * It contains a test for the 'parseUrlEntries' method.
 */
@MicronautTest
public class WikiURLExtractorUnitTest {

    /**
     * Verifies the 'parseUrlEntries' method for a non-empty page text.
     */
    @Test
    public void testParseUrlEntries_WhenPageTextHasValidEntry() {
        // Initialize the WikiURLExtractor
        WikiURLExtractor extractor = new WikiURLExtractor();

        // The URL element to use for the test
        String pageText = "Sample page text [http://www.example.com example]";

        // Expected URL entry
        Link expectedEntry = Link.newBuilder()
                .setUrl("http://www.example.com")
                .setDescription("example")
                .build();

        // Call the method to be tested
        List<Link> actualUrlEntries = extractor.parseUrlEntries(pageText);

        // Validate the result
        Assertions.assertEquals(1, actualUrlEntries.size());
        Assertions.assertEquals(expectedEntry, actualUrlEntries.get(0));
    }

    @Test
    public void testParseUrlEntries_WhenPageTextHasValidEntryForHttps() {
        // Initialize the WikiURLExtractor
        WikiURLExtractor extractor = new WikiURLExtractor();

        // The URL element to use for the test
        String pageText = "Sample page text [https://www.example.com example]";

        // Expected URL entry
        Link expectedEntry = Link.newBuilder()
                .setUrl("https://www.example.com")
                .setDescription("example")
                .build();

        // Call the method to be tested
        List<Link> actualUrlEntries = extractor.parseUrlEntries(pageText);

        // Validate the result
        Assertions.assertEquals(1, actualUrlEntries.size());
        Assertions.assertEquals(expectedEntry, actualUrlEntries.get(0));
    }

    /**
     * Verifies the 'parseUrlEntries' method for an empty page text.
     */
    @Test
    public void testParseUrlEntries_WhenPageTextIsEmpty() {
        // Initialize the WikiURLExtractor
        WikiURLExtractor extractor = new WikiURLExtractor();

        // The URL element to use for the test
        String pageText = "";

        // Call the method to be tested
        List<Link> actualUrlEntries = extractor.parseUrlEntries(pageText);

        // Validate the result
        Assertions.assertTrue(actualUrlEntries.isEmpty());
    }

    /**
     * Verifies the 'parseUrlEntries' method for a null page text.
     */
    @Test
    public void testParseUrlEntries_WhenPageTextIsNull() {
        // Initialize the WikiURLExtractor
        WikiURLExtractor extractor = new WikiURLExtractor();

        // The URL element to use for the test
        String pageText = null;

        // Call the method to be tested
        List<Link> actualUrlEntries = extractor.parseUrlEntries(pageText);

        // Validate the result
        Assertions.assertTrue(actualUrlEntries.isEmpty());
    }
}