package com.krickert.search.wiki.article.processor.component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import jakarta.validation.constraints.NotNull;

/**
 * The `ParagraphParser` class provides utility methods to parse a text into paragraphs.
 * This parser will consider numbered lists and bulleted lists as a single paragraph
 */
public class ParagraphParser {
    private final static Pattern LIST_PATTERN =
            Pattern.compile("^(\\t*((\\d+|\\b[a-z]\\b|\\b[A-Z]\\b|[ivxlcdm]+|[IVXLCDM]+)[.)]|[-*]))");

    public static List<String> splitIntoParagraphsAndRemoveEmpty(@NotNull String text) {
        StringBuilder builder = processLines(text);
        return splitIntoParagraphs(builder.toString());
    }

    private static StringBuilder processLines(@NotNull String text) {
        StringBuilder builder = new StringBuilder();
        String[] lines = text.split("\n");

        boolean lastLineWasListItem = false;

        for (String line : lines) {
            Matcher matcher = LIST_PATTERN.matcher(line);
            boolean isCurrentLineListItem = matcher.find();

            if (isCurrentLineListItem) {
                if (lastLineWasListItem) {
                    builder.append("\n").append(line);
                } else {
                    if (builder.length() != 0) {
                        builder.append("\n\n");
                    }
                    builder.append(line.substring(matcher.start()));
                }
                lastLineWasListItem = true;
            } else {
                if (builder.length() > 0) {
                    builder.append("\n\n");
                }
                builder.append(line.trim());
                lastLineWasListItem = false;
            }
        }
        return builder;
    }

    private static List<String> splitIntoParagraphs(String text) {
        return Arrays.stream(text.split("\\n\\s*\\n"))
                .filter(paragraph -> !paragraph.isBlank())
                .collect(Collectors.toList());
    }
}
