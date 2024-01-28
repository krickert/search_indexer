package com.krickert.search.crawler;

import com.google.common.collect.Maps;
import com.google.common.net.InternetDomainName;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import jakarta.inject.Singleton;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Singleton
public class WebCrawler {
    private static final Logger log = LoggerFactory.getLogger(WebCrawler.class);

    private final WebDriver driver;
    private Set<String> visitedUrls = new HashSet<>();

    private Map<String,String> data = Maps.newConcurrentMap();

    public WebCrawler(ResourceResolver resourceResolver) {

        Optional<URL> resource = resourceResolver.getLoader(ClassPathResourceLoader.class).get().getResource("ublock_CJPALHDLNBPAFIAMEJDNHCPHJBKEIAGM_1_54_0_0.crx");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        options.addExtensions(new File(resource.get().getFile()));
        options.addArguments("--headless=new");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(8, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(3, TimeUnit.SECONDS);
    }

    public void start(String url) {
        int CRAWL_DEPTH = 3;
        visitPage(url, CRAWL_DEPTH);
        driver.quit();
        log.info("Data:\n{}", data);
    }
    private void visitPage(String url, int crawlDepth) {
        try {
            visitPage(url, crawlDepth, new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    private void visitPage(String url, int crawlDepth, URL startUrl) {
        if (visitedUrls.contains(url) || url == null) {
            return;
        }
        InternetDomainName expectedDomainName = InternetDomainName.from(startUrl.getHost());
        final String topDomain = expectedDomainName.topPrivateDomain().toString();


        visitedUrls.add(url);
        driver.get(url);

        log.info("Crawling URL: {}", url);
        String bodyText = driver.findElement(By.tagName("body")).getText();
        data.put(url, bodyText);

        List<WebElement> links = driver.findElements(By.tagName("a"));

        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (isSameDomain(href, topDomain)) {
                if (!visitedUrls.contains(href)) {
                    visitPage(href, --crawlDepth, startUrl);
                }
            }
        }
    }
    public static boolean isSameDomain(String url, String expectedDomain) {
        try {
            java.net.URL netUrl = new java.net.URL(url);
            InternetDomainName domainName = InternetDomainName.from(netUrl.getHost());
            return domainName.hasPublicSuffix() && domainName.topPrivateDomain().toString().equals(expectedDomain);
        } catch (java.net.MalformedURLException e) {
            log.warn("Invalid URL in href",e);
            return false;
        }
    }

}
