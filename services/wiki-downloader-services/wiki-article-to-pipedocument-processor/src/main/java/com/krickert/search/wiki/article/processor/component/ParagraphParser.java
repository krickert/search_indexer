package com.krickert.search.wiki.article.processor.component;

import jakarta.validation.constraints.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
                    builder.append("\n")
                            .append(line.substring(matcher.start()).replaceAll("\\s+$", ""));
                } else {
                    if (!builder.isEmpty()) {
                        builder.append("\n\n");
                    }
                    builder.append(line.substring(matcher.start()).replaceAll("\\s+$", ""));
                }
                lastLineWasListItem = true;
            } else {
                if (!builder.isEmpty()) {
                    builder.append("\n\n");
                }
                builder.append(line.trim());
                lastLineWasListItem = false;
            }
        }

        return builder;
    }
    private static List<String> splitIntoParagraphs(String text) {
        Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
        Pattern numberPattern = Pattern.compile("^\\d+$"); // matches strings that are purely numeric

        return Arrays.stream(text.split("\\n\\s*\\n"))
                .filter(paragraph -> {
                    Matcher wordMatcher = wordPattern.matcher(paragraph);
                    Matcher numberMatcher = numberPattern.matcher(paragraph.trim()); // trim here to prevent leading/trailing spaces from affecting the match
                    return wordMatcher.find() && !numberMatcher.matches(); // check for presence of word, and absence of purely numeric string
                })
                .collect(Collectors.toList());
    }
}
