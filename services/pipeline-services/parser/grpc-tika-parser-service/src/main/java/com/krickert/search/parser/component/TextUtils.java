package com.krickert.search.parser.component;

public class TextUtils {
    public static String cleanUpText(String text) {
        StringBuilder output = new StringBuilder();
        String cleanedString;
        boolean isNewLineFlag = false;  // flag to track new lines
        boolean isSpaceFlag = false;    // flag to track whitespaces

        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (c == '\n' || c == '\r') {
                    if (!isNewLineFlag) {
                        output.append('\n');
                        isNewLineFlag = true;
                    }
                    isSpaceFlag = false;
                } else {
                    if (!isSpaceFlag) {
                        output.append(' ');
                        isSpaceFlag = true;
                    }
                    isNewLineFlag = false;
                }
            } else {
                output.append(c);
                isNewLineFlag = false;
                isSpaceFlag = false;
            }
        }

        cleanedString = output.toString().trim();
        return cleanedString;
    }
}
