package com.krickert.search.crawler;

import com.google.common.collect.Maps;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;

import jakarta.inject.Singleton;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
@Singleton
public class WebCrawler {
    private static final Logger log = LoggerFactory.getLogger(WebCrawler.class);

    private final WebDriver driver;
    private final ResourceResolver resourceResolver;
    private Set<String> visitedUrls = new HashSet<>();

    private Map<String,String> data = Maps.newConcurrentMap();

    public WebCrawler(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
        ;
        Optional<URL> resource = resourceResolver.getLoader(ClassPathResourceLoader.class).get().getResource("ublock_CJPALHDLNBPAFIAMEJDNHCPHJBKEIAGM_1_54_0_0.crx");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        options.addExtensions(new File(resource.get().getFile()));

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(8, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);
    }

    public void start(String url) {
        int CRAWL_DEPTH = 1;
        visitPage(url, CRAWL_DEPTH);
        driver.quit();
        log.info("Data:\n{}", data);
    }

    private void visitPage(String url, int crawlDepth) {
        if (visitedUrls.contains(url) || url == null) {
            return;
        }

        visitedUrls.add(url);
        driver.get(url);

        System.out.println("Crawling URL: " + url);

        data.put(url, driver.getPageSource());

        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.startsWith(url)) {
                if (crawlDepth != 0) {
                    visitPage(href, --crawlDepth);
                }
            }
        }
    }
}
