package com.krickert.search.wikipedia;

import com.krickert.search.installer.SolrInstallerOptions;
import com.krickert.search.opennlp.OrganizationExtractor;
import com.krickert.search.opennlp.PersonExtractor;
import edu.stanford.nlp.quoteattribution.Person;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OrganizationExtractor organizationExtractor;
    private final TokenizerModel tokenizerModel;
    private final PersonExtractor personExtractor;

    @Autowired
    public WikipediaArticleIndexer(WikipediaArticleMultipartDownloader wikipediaArticleMultipartDownloader,
                                   @Value("${parse.wikiresults}") Boolean isParseWikiResults,
                                   SolrInstallerOptions opts,
                                   OrganizationExtractor organizationExtractor,
                                   TokenizerModel tokenizerModel,
                                   PersonExtractor personExtractor) {
        this.wikipediaArticleMultipartDownloader = wikipediaArticleMultipartDownloader;
        this.opts = opts;
        this.isParseWikiResults = isParseWikiResults;
        this.organizationExtractor = organizationExtractor;
        this.tokenizerModel = tokenizerModel;
        this.personExtractor = personExtractor;
    }

    public void parseResultsToSolr() throws InterruptedException {
        if (!isParseWikiResults) {
            return;
        }
        ExecutorService fileParserExecutor = Executors.newFixedThreadPool(8);
        for (String file : wikipediaArticleMultipartDownloader.getPageDumpFiles()) {
            AsyncDumpFileProcessorRunnable runnable =
                    new AsyncDumpFileProcessorRunnable(file, opts.getSolrCollectionName(),
                            opts.getSolrUserName(),
                            opts.getSolrPassword(),
                            organizationExtractor,
                            tokenizerModel,
                            personExtractor);
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
