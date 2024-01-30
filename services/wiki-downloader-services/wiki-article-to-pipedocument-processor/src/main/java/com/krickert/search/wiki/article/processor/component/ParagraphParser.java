package com.krickert.search.wiki.article.processor.component;

import com.google.common.collect.Lists;
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

    /**
     * Splits the given text into sections based on the provided title.
     *
     * @param text  the input text to be split into sections
     * @param title the title to be used for each section
     * @return a list of sections, where each section starts with the title followed by the section content
     */
    public static List<String> splitIntoSections(@NotNull String text, @NotNull String title) {
        List<String> paragraphs = splitIntoParagraphsAndRemoveEmpty(text);
        List<String> sections = Lists.newArrayList();
        String sectionName = "summary";//first part of the section lists are always summaries
        String currentSection = "";//planceholder
        for (String paragraph : paragraphs) {
            if (paragraph.contains("\n")) {
                // only lists contain newlines
                sections.add(title + " " + sectionName + " " +  currentSection);
                currentSection = paragraph;
            } else if (paragraph.contains(".")) {
                currentSection +=  " " +  paragraph;
            } else {
                sections.add(title + " " + sectionName + currentSection);
                //no newline and no period - new section
                sectionName = paragraph;
                currentSection = "";
            }
        }
        sections.add(title + " " + sectionName + currentSection);
        return sections;
    }

    public static List<String> splitIntoParagraphsAndRemoveEmpty(@NotNull String text) {
        StringBuilder builder = processLines(text);
        String processedText = removeHttpSubstrings(builder.toString()); // HTTP substrings removed here
        return splitIntoParagraphs(processedText).stream()
                .map(ParagraphParser::cleanWhiteSpace) // cleanWhiteSpace() method applied here
                .collect(Collectors.toList());
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
    private static String removeHttpSubstrings(String text) {
        StringBuilder output = new StringBuilder();
        int start = 0;
        while (true) {
            int httpIndex = text.indexOf("[http", start);
            if (httpIndex == -1) {
                output.append(text.substring(start));
                break;
            }
            int closingBracketIndex = text.indexOf(']', httpIndex);
            if (closingBracketIndex == -1) {
                output.append(text.substring(start));
                break;
            }
            output.append(text.substring(start, httpIndex));
            start = closingBracketIndex + 1;
        }
        return output.toString();
    }

    private static String cleanWhiteSpace(String text) {
        if (text.contains("\n")) {
            // Process list lines separately
            return Arrays.stream(text.split("\n"))
                    .map(line -> line.replaceAll("(?<=\\S)\\s+", " ")) // replace whitespaces that are not leading the line
                    .collect(Collectors.joining("\n"));
        }

        StringBuilder output = new StringBuilder();
        boolean isLastCharacterSpace = false;

        for (char c : text.toCharArray()) {
            boolean isCurrentCharacterSpace = Character.isWhitespace(c);

            if (isLastCharacterSpace && isCurrentCharacterSpace) {
                // Ignore this space
                continue;
            }

            output.append(c);
            isLastCharacterSpace = isCurrentCharacterSpace;
        }

        return output.toString();
    }

    public enum ParagraphStrategy {
        PARAGRAPH, SECTION;
    }
}
