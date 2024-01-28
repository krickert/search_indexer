package com.krickert.search.wiki.article.processor.component;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for `ParagraphParser` class.
 * <p>
 * This class contains tests for `splitIntoParagraphsAndRemoveEmpty` method 
 * of the `ParagraphParser` class. 
 */
public class ParagraphParserTest {

    /**
     * This test checks the functionality of `splitIntoParagraphsAndRemoveEmpty` method when a string with empty paragraph is given.
     */
    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithEmptyParagraphs() {
        String textWithEmptyParagraphs = "This is a paragraph.\n\n\nThis is another paragraph.";
        List<String> expectedList = Arrays.asList("This is a paragraph.", "This is another paragraph.");
        
        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(textWithEmptyParagraphs);
        
        assertEquals(expectedList, result);
    }

    /**
     * This test checks the functionality of `splitIntoParagraphsAndRemoveEmpty` method when a string with no empty paragraph is given.
     */
    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithNoEmptyParagraphs() {
        String text = "This is a paragraph.\nThis is another paragraph.";
        List<String> expectedList = Arrays.asList("This is a paragraph.", "This is another paragraph.");
        
        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(text);
        
        assertEquals(expectedList, result);
    }

    /**
     * This test checks the functionality of `splitIntoParagraphsAndRemoveEmpty` method when an empty string is given.
     */
    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithEmptyString() {
        String emptyText = "";
        List<String> expectedList = List.of();
        
        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(emptyText);
        
        assertEquals(expectedList, result);
    }

    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithNoEmptyParagraphsAndNumberedLists() {
        String text = "This is a paragraph.\nThis is another paragraph.\n\t1. This is the third paragraph\n\t2. Which should be part of the above paragraph. \n\t3. Which is part of this paragraph again.\n\t\ta. Subtab a\n\t\tb. Subtab b\n\t4. Element 4 and end of list\nThis is a new paragraph though so it should be another element.";
        List<String> expectedList = Arrays.asList("This is a paragraph.", "This is another paragraph.", "\t1. This is the third paragraph\n\t2. Which should be part of the above paragraph.\n\t3. Which is part of this paragraph again.\n\t\ta. Subtab a\n\t\tb. Subtab b\n\t4. Element 4 and end of list","This is a new paragraph though so it should be another element.");

        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(text);

        assertEquals(expectedList, result);
    }

    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithNoEmptyParagraphsAndRomanNumberLists() {
        String text = "This is a paragraph.\nThis is another paragraph.\n\ti. This is the third paragraph\n\tii. Which should be part of the above paragraph.     \n\tiii. Which is part of this paragraph again.\n\t\t1. Subtab a\n\t\t2. Subtab b\n\tiv. Element 4 and end of list       \n\n\n\n   This is a new paragraph though so it should be another element.";
        List<String> expectedList = Arrays.asList("This is a paragraph.", "This is another paragraph.", "\ti. This is the third paragraph\n\tii. Which should be part of the above paragraph.\n\tiii. Which is part of this paragraph again.\n\t\t1. Subtab a\n\t\t2. Subtab b\n\tiv. Element 4 and end of list","This is a new paragraph though so it should be another element.");

        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(text);

        assertEquals(expectedList, result);
    }

    @Test
    public void testSplitIntoParagraphsAndRemoveEmptyWithEmptyParagraphsHashTagLines() {
        String textWithEmptyParagraphs = "This is a paragraph.\n\n\nThis is another paragraph.\n%\n^&$#\n\n34131432131241\n#\n#\n\nThis is the last one.";
        List<String> expectedList = Arrays.asList("This is a paragraph.", "This is another paragraph.", "This is the last one.");

        List<String> result = ParagraphParser.splitIntoParagraphsAndRemoveEmpty(textWithEmptyParagraphs);

        assertEquals(expectedList, result);
    }


}