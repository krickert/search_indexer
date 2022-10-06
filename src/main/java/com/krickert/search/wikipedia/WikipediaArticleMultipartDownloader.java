package com.krickert.search.wikipedia;

import com.google.common.collect.Lists;
import com.krickert.util.download.AsyncDownloaderRunnable;
import com.krickert.util.download.Downloader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class WikipediaArticleMultipartDownloader {
    final Logger logger = LoggerFactory.getLogger(WikipediaArticleMultipartDownloader.class);
    final List<String> wikiPageDumpFiles = Lists.newArrayListWithExpectedSize(130);
    final private ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final Integer maxTries;
    private final String wikipediaDumpUrlPrefix;
    private final String workspaceLocation;
    private final Boolean isDownloadWikiMultipart;
    private final Downloader downloader;
    private final String wikipediaMd5UrlString;

    @Autowired
    public WikipediaArticleMultipartDownloader(
            @Value("${download.maxTries}") Integer maxTries,
            @Value("${wikepedia.multipart.url.prefix}") String wikipediaDumpUrlPrefix,
            @Value("${workspace.location}") String workspaceLocation,
            @Value("${download.wikimultipart}") Boolean isDownloadWikiMultipart,
            @Value("${wikipedia.multipart.current.md5}") String wikipediaMd5UrlString,
            Downloader downloader) {
        this.maxTries = maxTries;
        this.wikipediaDumpUrlPrefix = wikipediaDumpUrlPrefix;
        this.workspaceLocation = workspaceLocation;
        this.downloader = downloader;
        this.wikipediaMd5UrlString = wikipediaMd5UrlString;
        this.isDownloadWikiMultipart = isDownloadWikiMultipart;
    }

    private static boolean isMultiStreamArticleFile(String fileName) {
        return fileName.contains("pages-articles-multistream") &&
                !fileName.contains("pages-articles-multistream.xml.bz2") &&
                !fileName.contains("index");
    }

    private static boolean isMultiStreamIndexFile(String fileName) {
        return fileName.contains("pages-articles-multistream-index") &&
                !fileName.contains("pages-articles-multistream-index.txt.bz2");
    }

    public Collection<String> getPageDumpFiles() {
        return this.wikiPageDumpFiles;
    }

    public void downloadWikipediaMultipart() {
        if (!isDownloadWikiMultipart) {
            return;
        }
        final File md5WikipediaFile = getWikipediaLatestFileList();
        List<String[]> entries = parseMd5File(md5WikipediaFile);
        for (String[] entry : entries) {
            logger.info("MD5: " + entry[0] + "\t file: " + entry[1]);
            String date = parseWikiDateFromFile(entry[1]);
            try {
                URL fileToDownload = new URL(wikipediaDumpUrlPrefix + date + '/' + entry[1]);
                String destFile = workspaceLocation + entry[1];
                AsyncDownloaderRunnable asyncDownloaderRunnable = new AsyncDownloaderRunnable(fileToDownload, destFile, entry[0], maxTries);
                executorService.execute(asyncDownloaderRunnable);
                Thread.sleep(100L);
                wikiPageDumpFiles.add(destFile);
            } catch (MalformedURLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.shutdown();
        while (true) {
            try {
                if (executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    break;
                } else {
                    logger.info("Waiting for termination");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private String parseWikiDateFromFile(String wikiFileName) {
        return StringUtils.substringBetween(wikiFileName, "enwiki-", "-pages");
    }

    private File getWikipediaLatestFileList() {
        final File md5WikipediaFile;
        try {
            md5WikipediaFile = downloader.download(new URL(wikipediaMd5UrlString), new File(workspaceLocation + "/wikiList.md5"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return md5WikipediaFile;
    }

    private List<String[]> parseMd5File(File md5WikipediaFile, WIKI_FILE_TYPE fileType) {
        List<String[]> lines = Lists.newArrayListWithExpectedSize(500);
        try (BufferedReader reader = new BufferedReader(new FileReader(md5WikipediaFile))) {

            String line = reader.readLine();
            String[] data;
            while (line != null) {
                data = line.split(" {2}");
                String fileName = data[1];
                if (isFileType(fileName, fileType)) {
                    lines.add(data);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lines;
    }

    private boolean isFileType(String fileName, WIKI_FILE_TYPE fileType) {
        if (fileType == WIKI_FILE_TYPE.MULTISTREAM) {
            return isMultiStreamIndexFile(fileName) ||
                    isMultiStreamArticleFile(fileName);
        } else if (fileType == WIKI_FILE_TYPE.ARTICLE) {
            return isArticleFile(fileName);
        } else {
            return false;
        }
    }

    private boolean isArticleFile(String fileName) {
        return fileName.contains("pages-article") &&
                !fileName.contains("pages-articles.xml.bz2");
    }

    private List<String[]> parseMd5File(File md5WikipediaFile) {
        List<String[]> lines = parseMd5File(md5WikipediaFile, WIKI_FILE_TYPE.MULTISTREAM);
        if (lines.size() == 0) {
            logger.info("multistream is not yet available.  backing up to articles.");
        }
        lines = parseMd5File(md5WikipediaFile, WIKI_FILE_TYPE.ARTICLE);
        return lines;
    }

    private enum WIKI_FILE_TYPE {
        MULTISTREAM, ARTICLE
    }

}
