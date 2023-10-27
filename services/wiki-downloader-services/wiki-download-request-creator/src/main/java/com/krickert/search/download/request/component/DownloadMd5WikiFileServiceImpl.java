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

@Singleton
@Requires(notEnv = Environment.TEST)
public class DownloadMd5WikiFileServiceImpl implements DownloadMd5WikiFileService {
    private static final Logger log = LoggerFactory.getLogger(DownloadMd5WikiFileServiceImpl.class);

    final String wikiDownloadName;
    final Rx3HttpClient client;
    final boolean freshCopy;

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

    @Override
    public String downloadWikiMd5AsString(String fileList) {

        final String wikiFileName;
        if (isNotEmpty(fileList)) {
            // the file was specified in the commandline
            //if the file doesn't exist, an exception is thrown
            Optional<String> fileInfo = readFileAsString(fileList);
            if (fileInfo.isPresent()) {
                return fileInfo.get();
            } else {
                wikiFileName = fileList;
            }
        } else if (isNotEmpty(wikiDownloadName) && !freshCopy) {
            Optional<String> configFileContents = readFileAsString(wikiDownloadName);
            if (configFileContents.isPresent()) {
                return configFileContents.get();
            } else {
                wikiFileName = wikiDownloadName;
            }
        } else if (freshCopy && isNotEmpty(wikiDownloadName)) {
            wikiFileName = wikiDownloadName;
        } else {
            wikiFileName = "wikiList.md5";
        }

        log.info("Config file is not present for download.  Downloading a fresh copy onto disk.");
        String fileContents = retrieveWikiDumpFileContentsFromWikipedia();
        try {
            FileUtils.writeStringToFile(new File(wikiFileName), fileContents, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileContents;
    }

    public String retrieveWikiDumpFileContentsFromWikipedia() {
        return client.retrieve(
                GET("enwiki-latest-md5sums.txt"),
                String.class).blockingFirst();
    }


}
