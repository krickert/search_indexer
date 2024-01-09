package com.krickert.search.crawler;

import com.krickert.search.model.crawler.CrawlPageRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class WebCrawlerManager {

    private final ConcurrentUnboundedNonRepeatingQueue<CrawlEntry> queue;
    private final WebCrawler webCrawler;

    @Inject
    public WebCrawlerManager(WebCrawler webCrawler) {
        this.webCrawler = webCrawler;
        this.queue = new ConcurrentUnboundedNonRepeatingQueue<CrawlEntry>();
    }


    public void crawlWebsite(CrawlPageRequest request) {
        queue.add(new CrawlEntry(request.getUrl()));
    }



}
