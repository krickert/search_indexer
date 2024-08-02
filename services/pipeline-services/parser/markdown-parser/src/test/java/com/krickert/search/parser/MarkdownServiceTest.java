package com.krickert.search.parser;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@MicronautTest
public class MarkdownServiceTest {

    @Test
    void testParseMarkdownFromInputStream() {
        MarkdownService markdownService = new MarkdownService();
        String markdown = "# Heading\n\nParagraph";
        InputStream stream = new java.io.ByteArrayInputStream(markdown.getBytes(UTF_8));

        try {
            List<Section> sections = markdownService.parseMarkdown(stream);
            Assertions.assertEquals(2, sections.size());
            Assertions.assertEquals("heading", sections.get(0).type());
            Assertions.assertEquals("Heading", sections.get(0).body());
            Assertions.assertEquals(1, sections.get(0).headingIndentation());
            Assertions.assertEquals("paragraph", sections.get(1).type());
            Assertions.assertEquals("Paragraph", sections.get(1).body());
            Assertions.assertEquals(-1, sections.get(1).headingIndentation());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testParseMarkdownFromString() {
        MarkdownService markdownService = new MarkdownService();
        String markdown = "# Heading\n\nParagraph";

        List<Section> sections = markdownService.parseMarkdown(markdown);
        Assertions.assertEquals(2, sections.size());
        Assertions.assertEquals("heading", sections.get(0).type());
        Assertions.assertEquals("Heading", sections.get(0).body());
        Assertions.assertEquals(1, sections.get(0).headingIndentation());
        Assertions.assertEquals("paragraph", sections.get(1).type());
        Assertions.assertEquals("Paragraph", sections.get(1).body());
        Assertions.assertEquals(-1, sections.get(1).headingIndentation());
    }

    @Test
    void testParseMarkdownFromFile() {
        MarkdownService markdownService = new MarkdownService();
        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("markdowntest", ".md");
            java.nio.file.Files.write(tempFile.toPath(), "# Heading\n\nParagraph".getBytes());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        List<Section> sections = markdownService.parseMarkdown(tempFile);
        Assertions.assertEquals(2, sections.size());
        Assertions.assertEquals("heading", sections.get(0).type());
        Assertions.assertEquals("Heading", sections.get(0).body());
        Assertions.assertEquals(1, sections.get(0).headingIndentation());
        Assertions.assertEquals("paragraph", sections.get(1).type());
        Assertions.assertEquals("Paragraph", sections.get(1).body());
        Assertions.assertEquals(-1, sections.get(1).headingIndentation());
    }

}