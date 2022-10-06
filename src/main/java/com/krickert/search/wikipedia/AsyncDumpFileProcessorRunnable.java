package com.krickert.search.wikipedia;

import info.bliki.wiki.dump.WikiXMLParser;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncDumpFileProcessorRunnable implements Runnable {

    final String file;
    final SolrArticleFilter solrArticleFilter;
    Logger logger = LoggerFactory.getLogger(AsyncDumpFileProcessorRunnable.class);

    public AsyncDumpFileProcessorRunnable(String file, String collectionName) {
        this.file = file;
        BlockingQueue<SolrInputDocument> documents = new LinkedBlockingQueue<>();
        AsyncSolrIndexerRunnable solrIndexer = new AsyncSolrIndexerRunnable(documents, collectionName);
        this.solrArticleFilter = new SolrArticleFilter(documents, solrIndexer);
        logger.info("queued up file for processing: {}", file);
    }

    @Override
    public void run() {
        WikiXMLParser parser;
        try {
            parser = new WikiXMLParser(new File(file), solrArticleFilter);
            parser.parse();
            solrArticleFilter.stopListeningForSolrUpdates();
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
