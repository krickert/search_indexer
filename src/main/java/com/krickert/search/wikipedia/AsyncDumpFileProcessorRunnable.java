package com.krickert.search.wikipedia;

import com.krickert.search.opennlp.NlpExtractor;
import com.krickert.search.opennlp.OrganizationExtractor;
import com.krickert.search.opennlp.PersonExtractor;
import info.bliki.wiki.dump.WikiXMLParser;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
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
    private final Integer docBufferSize;
    Logger logger = LoggerFactory.getLogger(AsyncDumpFileProcessorRunnable.class);

    public AsyncDumpFileProcessorRunnable(String file, String collectionName,
                                          String username, String password,
                                          TokenizerModel tokenModel,
                                          TokenNameFinderModel orgModel,
                                          TokenNameFinderModel personModel,
                                          TokenNameFinderModel locationModel,
                                          TokenNameFinderModel dateModel,
                                          Integer docBufferSize) {
        this.file = file;
        this.docBufferSize = docBufferSize;
        this.username = username;
        this.password = password;
        OrganizationExtractor organizationExtractor = new OrganizationExtractor(tokenModel, orgModel);
        PersonExtractor personExtractor = new PersonExtractor(tokenModel, personModel);
        NlpExtractor locationExtractor = new NlpExtractor(tokenModel,locationModel);
        NlpExtractor dateExtractor = new NlpExtractor(tokenModel,dateModel);

        BlockingQueue<SolrInputDocument> documents = new LinkedBlockingQueue<>();
        AsyncSolrIndexerRunnable solrIndexer = new AsyncSolrIndexerRunnable(documents, collectionName, username, password, docBufferSize);
        this.solrArticleFilter = new SolrArticleFilter(documents, solrIndexer, organizationExtractor, personExtractor, locationExtractor, dateExtractor, new TokenizerME(tokenModel));
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
