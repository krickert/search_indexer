package com.krickert.search.wikipedia;

import com.krickert.search.installer.SolrInstallerOptions;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class WikipediaArticleIndexer {
    private final Logger logger = LoggerFactory.getLogger(WikipediaArticleMultipartDownloader.class);
    private final WikipediaArticleMultipartDownloader wikipediaArticleMultipartDownloader;
    private final SolrInstallerOptions opts;
    private final Boolean isParseWikiResults;
    private final TokenizerModel tokenizerModel;
    private final TokenNameFinderModel personModel;
    private final TokenNameFinderModel orgModel;
    private final TokenNameFinderModel locationModel;
    private final TokenNameFinderModel dateModel;
    private final Integer docBufferSize;
    private final Integer pollTimeout;
    private final Integer numberOfFileParsers;

    @Autowired
    public WikipediaArticleIndexer(WikipediaArticleMultipartDownloader wikipediaArticleMultipartDownloader,
                                   @Value("${parse.wikiresults}") Boolean isParseWikiResults,
                                   SolrInstallerOptions opts,
                                   TokenizerModel tokenizerModel,
                                   @Qualifier("organizationFinder") TokenNameFinderModel orgModel,
                                   @Qualifier("personFinder")TokenNameFinderModel personModel,
                                   @Qualifier("locationFinder")TokenNameFinderModel locationModel,
                                   @Qualifier("dateFinder")TokenNameFinderModel dateModel,
                                   @Value("${solr.thread.doc.buffersize}")Integer docBufferSize,
                                   @Value("${solr.polltimeout}")Integer pollTimeout,
                                   @Value("${solr.thread.file.parsers}")Integer numberOfFileParsers) {
        this.wikipediaArticleMultipartDownloader = wikipediaArticleMultipartDownloader;
        this.opts = opts;
        this.isParseWikiResults = isParseWikiResults;
        this.orgModel = orgModel;
        this.tokenizerModel = tokenizerModel;
        this.personModel = personModel;
        this.locationModel = locationModel;
        this.dateModel = dateModel;
        this.docBufferSize = docBufferSize;
        this.numberOfFileParsers = numberOfFileParsers;
        this.pollTimeout = pollTimeout;
    }

    public void parseResultsToSolr() throws InterruptedException {
        if (!isParseWikiResults) {
            return;
        }
        ExecutorService fileParserExecutor = Executors.newFixedThreadPool(numberOfFileParsers);
        for (String file : wikipediaArticleMultipartDownloader.getPageDumpFiles()) {
            AsyncDumpFileProcessorRunnable runnable =
                    new AsyncDumpFileProcessorRunnable(file, opts.getSolrCollectionName(),
                            opts.getSolrUserName(),
                            opts.getSolrPassword(),
                            tokenizerModel,
                            orgModel,
                            personModel,
                            locationModel,
                            dateModel,
                            docBufferSize,
                            pollTimeout);
            fileParserExecutor.execute(runnable);
        }
        //at this point all of them should be in the queue.
        fileParserExecutor.shutdown();
        logger.info("all files queued up.  awaiting for all threads to terminate.");
        int iterations = 0;
        while (!fileParserExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
            int secondsElapsed = ++iterations * 10;
            logger.info("STILL PROCESSING FILES FOR {} SECONDS", secondsElapsed);
        }
    }
}
