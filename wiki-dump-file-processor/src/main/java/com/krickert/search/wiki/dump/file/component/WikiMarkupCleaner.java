package com.krickert.search.wiki.dump.file.component;

import jakarta.inject.Singleton;
import org.wikiclean.WikiClean;

@Singleton
public class WikiMarkupCleaner {
    final static String xmlStartTag = "<text xml:space=\"preserve\">";
    final static String xmlEndTag = "</text>";
    private final static WikiClean cleaner = new WikiClean.Builder().withFooter(true).withTitle(false).build();

    public String extractCleanTestFromWiki(String pageText) {
        return cleaner.clean(xmlStartTag + pageText + xmlEndTag);
    }
}
