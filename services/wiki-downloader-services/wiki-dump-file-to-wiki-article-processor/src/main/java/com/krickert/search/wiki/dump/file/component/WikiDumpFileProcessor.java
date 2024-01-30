package com.krickert.search.wiki.dump.file.component;

import com.krickert.search.model.wiki.DownloadedFile;
import info.bliki.wiki.dump.WikiXMLParser;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
@Singleton
public class WikiDumpFileProcessor {
    private static final Logger log = LoggerFactory.getLogger(WikiDumpFileProcessor.class);
    private final WikiArticleFilter wikiArticleFilter;

    @Inject
    public WikiDumpFileProcessor(WikiArticleFilter wikiArticleFilter) {
        this.wikiArticleFilter = wikiArticleFilter;
    }

    public void process(DownloadedFile request) {
        final WikiXMLParser parser;
        try {
            parser = new WikiXMLParser(new File(request.getFullFilePath()), wikiArticleFilter);
            parser.parse();
        } catch (IOException | SAXException e) {
            log.error("Problem with parsing file {}.  Error returned: {}", request, ExceptionUtils.getStackTrace(e));
        }
    }
}
