package com.krickert.search.download.request.component;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.krickert.search.download.request.util.MicronautFileUtil.readFileAsString;
import static io.micronaut.http.HttpRequest.GET;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * This class represents a service used to download an MD5 file from Wikipedia.
 */
@Singleton
@Requires(notEnv = Environment.TEST)
public class DownloadMd5WikiFileServiceImpl implements DownloadMd5WikiFileService {
    private static final Logger log = LoggerFactory.getLogger(DownloadMd5WikiFileServiceImpl.class);
    /**
     * Represents the default filename for the MD5 file collected from Wikipedia.
     */
    public static final String DEFAULT_WIKI_MD5_FILENAME = "wikiList.md5";
    /**
     * Represents the name of the file to be downloaded from Wikipedia.
     */
    public static final String DOWNLOAD_WIKI_FILENAME = "enwiki-20240120-md5sums.txt";

    /**
     * Represents the name of the wiki file for downloading.
     */
    final String wikiDownloadName;
    /**
     * This variable represents an instance of the Rx3HttpClient class.
     * It is used for making HTTP requests and retrieving data from the internet.
     */
    final Rx3HttpClient client;
    /**
     * Represents a service used to download a MD5 file from Wikipedia.
     */
    final boolean freshCopy;

    /**
     * Represents a service used to download a MD5 file from Wikipedia.
     */
    @Inject
    public DownloadMd5WikiFileServiceImpl(
            @Value("${wikipedia.download-name}")
            String wikiDownloadName,
            @Value("${wikipedia.fresh-copy}") boolean freshCopy,
            @Client("${wikipedia.md5-url}")
            Rx3HttpClient client) {
        this.wikiDownloadName = wikiDownloadName;
        this.client = client;
        this.freshCopy = freshCopy;
    }

    /**
     * Downloads the MD5 file from Wikipedia as a string.
     *
     * @param fileList the name of the file to be downloaded
     * @return the contents of the downloaded file as a string
     */
    @Override
    public String downloadWikiMd5AsString(String fileList) {
        if (isNotEmpty(fileList)) {
            Optional<String> fileInfo = readFileAsString(fileList);
            if (fileInfo.isPresent()) {
                return fileInfo.get();
            }
        } else if (isNotEmpty(wikiDownloadName) && !freshCopy) {
            Optional<String> configFileContents = readFileAsString(wikiDownloadName);
            if (configFileContents.isPresent()) {
                return configFileContents.get();
            }
        }
        String wikiFileName = getWikiFileName(fileList);
        log.info("Config file is not present for download.  Downloading a fresh copy onto disk.");
        String fileContents = retrieveWikiDumpFileContentsFromWikipedia();
        try {
            FileUtils.writeStringToFile(new File(wikiFileName), fileContents, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContents;
    }

    /**
     * Returns the name of the wiki file based on the given file list.
     *
     * @param fileList the name of the file list
     * @return the name of the wiki file
     */
    private String getWikiFileName(String fileList) {
        if (isNotEmpty(fileList)) {
            return fileList;
        } else if (isNotEmpty(wikiDownloadName) && !freshCopy) {
            return wikiDownloadName;
        } else if (freshCopy && isNotEmpty(wikiDownloadName)) {
            return wikiDownloadName;
        } else {
            return DEFAULT_WIKI_MD5_FILENAME;
        }
    }

    /**
     * Retrieves the contents of a Wikipedia dump file from the internet.
     *
     * @return The contents of the Wikipedia dump file as a string.
     */
    public String retrieveWikiDumpFileContentsFromWikipedia() {
        return client.retrieve(
                GET(DOWNLOAD_WIKI_FILENAME),
                String.class).blockingFirst();
    }


}
