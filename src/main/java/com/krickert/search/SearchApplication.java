package com.krickert.search;

import com.krickert.search.installer.SolrInstaller;
import com.krickert.search.wikipedia.WikipediaArticleBitorrentDownloader;
import com.krickert.search.wikipedia.WikipediaArticleIndexer;
import com.krickert.search.wikipedia.WikipediaArticleMultipartDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.krickert")
public class SearchApplication implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(SearchApplication.class);
    private final WikipediaArticleBitorrentDownloader wikipediaArticleBitorrentDownloader;
    private final WikipediaArticleMultipartDownloader wikipediaArticleMultipartDownloader;
    private final SolrInstaller solrInstaller;
    private final WikipediaArticleIndexer wikipediaArticleIndexer;


    @Autowired
    public SearchApplication(WikipediaArticleBitorrentDownloader wikipediaArticleBitorrentDownloader,
                             WikipediaArticleMultipartDownloader wikipediaArticleMultipartDownloader,
                             SolrInstaller solrInstaller,
                             WikipediaArticleIndexer wikipediaArticleIndexer) {
        this.wikipediaArticleBitorrentDownloader = wikipediaArticleBitorrentDownloader;
        this.wikipediaArticleMultipartDownloader = wikipediaArticleMultipartDownloader;
        this.solrInstaller = solrInstaller;
        this.wikipediaArticleIndexer = wikipediaArticleIndexer;
    }

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking solr installation");
        solrInstaller.installSolr();

        logger.info("Checking for wikipedia files.  Downloading if needed.");
        wikipediaArticleBitorrentDownloader.downloadWikipediaTorrent();
        wikipediaArticleMultipartDownloader.downloadWikipediaMultipart();

        logger.info("Parsing the results to solr");
        wikipediaArticleIndexer.parseResultsToSolr();
    }


}
