package com.krickert.search.parser;

import jakarta.inject.Singleton;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Singleton
public class MarkdownService {

    public List<Section> parseMarkdown(InputStream inputStream) throws IOException {
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {

            scanner.useDelimiter("\\A");
            String wholeContent = scanner.hasNext() ? scanner.next() : "";

            // Use parser to parse markdown content directly
            Parser parser = Parser.builder().build();
            Node document = parser.parse(wholeContent);

            // Prepare the structure for sections
            List<Section> sections = new ArrayList<>();

            // Visit nodes and extract text
            Node node = document.getFirstChild();
            while (node != null) {
                if (node instanceof Heading heading) {
                    String headingText = TextContentRenderer.builder().build().render(heading);
                    sections.add(new Section("heading", headingText.trim(), heading.getLevel()));
                } else if (node instanceof Paragraph paragraph) {
                    String paragraphText = TextContentRenderer.builder().build().render(paragraph);
                    sections.add(new Section("paragraph", paragraphText.trim(), -1));
                }
                node = node.getNext();
            }

            return sections;
        }
    }

    public List<Section> parseMarkdown(String markdown) {
        InputStream stream = new ByteArrayInputStream(markdown.getBytes(StandardCharsets.UTF_8));
        try {
            return parseMarkdown(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Section> parseMarkdown(File file) {
        try (InputStream stream = new FileInputStream(file)) {
            return parseMarkdown(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse markdown from file", e);
        }
    }
}