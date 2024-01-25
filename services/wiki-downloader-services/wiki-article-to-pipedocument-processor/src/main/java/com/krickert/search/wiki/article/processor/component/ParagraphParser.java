package com.krickert.search.wiki.article.processor.component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import jakarta.validation.constraints.NotNull;

/**
 * The `ParagraphParser` class provides utility methods to parse a text into paragraphs.
 * This parser will consider numbered lists and bulleted lists as a single paragraph
 */
public class ParagraphParser {
    private static final Pattern LIST_PATTERN = Pattern.compile("^(\\s*((\\d+|\\b[a-z]\\b|\\b[A-Z]\\b|[ivxlcdm]+|[IVXLCDM]+)[-.)]|[-*]))");

    public static List<String> splitIntoParagraphsAndRemoveEmpty(@NotNull String text) {
        StringBuilder builder = processLines(text);
        return splitIntoParagraphs(builder.toString());
    }

    private static StringBuilder processLines(@NotNull String text) {
        StringBuilder builder = new StringBuilder();
        String[] lines = text.split("\n");

        boolean lastLineWasListItem = false;

        for (String line : lines) {
            if (LIST_PATTERN.matcher(line).find()) {
                if (lastLineWasListItem) {
                    // continue the list in the same paragraph
                    builder.append("\n").append(line);
                } else {
                    if (builder.length() != 0) {
                        // if not the beginning, add blank line to mark start of a new paragraph
                        builder.append("\n\n");
                    }
                    // append list item line as it is
                    builder.append(line);
                }
                lastLineWasListItem = true;
            } else {
                // regular line, not part of a list; so append as a separate paragraph
                if (builder.length() != 0) {
                    builder.append("\n\n");
                }
                // append line after trimming leading and trailing whitespace
                builder.append(line.trim());
                lastLineWasListItem = false;
            }
        }
        return builder;
    }

    private static List<String> splitIntoParagraphs(String text) {
        return Arrays.stream(text.split("\\n\\s*\\n"))
                .filter(paragraph -> !paragraph.strip().isEmpty())
                .collect(Collectors.toList());
    }
}
