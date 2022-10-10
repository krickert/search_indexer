package com.krickert.search.wikipedia;

import com.krickert.search.opennlp.OrganizationExtractor;
import com.krickert.search.opennlp.PersonExtractor;
import info.bliki.wiki.dump.WikiXMLParser;
import opennlp.tools.tokenize.TokenizerModel;
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
    final String username;
    final String password;
    Logger logger = LoggerFactory.getLogger(AsyncDumpFileProcessorRunnable.class);

    final OrganizationExtractor organizationExtractor;
    public AsyncDumpFileProcessorRunnable(String file, String collectionName,
                                          String username, String password,
                                          OrganizationExtractor organizationExtractor,
                                          TokenizerModel tokenModel,
                                          PersonExtractor personExtractor) {
        this.file = file;
        this.username = username;
        this.password = password;
        this.organizationExtractor = organizationExtractor;
        BlockingQueue<SolrInputDocument> documents = new LinkedBlockingQueue<>();
        AsyncSolrIndexerRunnable solrIndexer = new AsyncSolrIndexerRunnable(documents, collectionName, username, password);
        this.solrArticleFilter = new SolrArticleFilter(documents, solrIndexer, organizationExtractor, personExtractor, tokenModel);
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
            logger.error("couldn't parse " + file, e);
            throw new RuntimeException(e);
        }
    }

}
