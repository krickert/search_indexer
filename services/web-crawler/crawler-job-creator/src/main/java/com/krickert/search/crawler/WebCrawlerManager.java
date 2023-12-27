package com.krickert.search.crawler;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WebCrawlerManager {

    private final WebCrawler webCrawler;
    private final ConcurrentUnboundedNonRepeatingQueue<CrawlEntry> queue;

    @Inject
    public WebCrawlerManager(WebCrawler webCrawler) {
        this.webCrawler = webCrawler;
        this.queue = new ConcurrentUnboundedNonRepeatingQueue<CrawlEntry>();
    }





}
