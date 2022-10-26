package com.krickert.search.download.request;


import com.krickert.search.model.DownloadFileRequest;
import com.krickert.search.model.ErrorCheckType;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.client.annotation.*;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;

import static io.micronaut.http.HttpRequest.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Command(name = "populate-file-requests",
        description = "checks the wikipedia page for downloads and adds new files to the queue for download.",
        mixinStandardHelpOptions = true, version = "POS")
public class SendFileRequestsCommand implements Runnable {

    @Client("${download.request-url}")
    @Inject
    Rx3HttpClient client;
    @Option(names = {"-v", "--verbose"}, description = "Shows some project details")
    boolean verbose;
    @Value("${wikipedia.md5-url}")
    String wikipediaMd5UrlString;
    @Value("${wikipedia.download-location}")
    String workspaceHome;
    @Value("${wikipedia.prefix-url}")
    String wikipediaPrefixUrl;
    @Inject
    DownloadRequestProducer producer;

    public static void main(String[] args) {
        int exitCode = PicocliRunner.execute(SendFileRequestsCommand.class, args);
        System.exit(exitCode);
    }

    public void run() {
        String m = downloadWikiMd5AsString();
        Collection<String[]> fileList = parseFileList(m);
        if (verbose) {
            log.info(m);
            log.info(fileList.size() + " files found");
        }
        log.info(workspaceHome);
        log.info(wikipediaMd5UrlString);
        log.info(wikipediaPrefixUrl);
        Collection<DownloadFileRequest> sendMe = createDownloadRequests(fileList);
        log.info("here: " + sendMe);
        for(DownloadFileRequest request : sendMe) {
            producer.sendDownloadRequest(UUID.randomUUID(), request);
        }

    }

    private String downloadWikiMd5AsString() {
        String m = client.retrieve(
                GET("enwiki-latest-md5sums.txt"),
                String.class).blockingFirst();
        return m;
    }

    private Collection<DownloadFileRequest> createDownloadRequests(Collection<String[]> fileList) {
        Collection<DownloadFileRequest> response = new ArrayList<>(fileList.size());
        for(String[] data : fileList) {
            if (ArrayUtils.isNotEmpty(data) && data.length == 2) {
                String dumpFileDateStr = parseWikiDateFromFile(data[1]);
                response.add(DownloadFileRequest.newBuilder()
                        .setErrorType(ErrorCheckType.MD5)
                        .setErrorcheck(data[0])
                        .setURL(this.wikipediaPrefixUrl + dumpFileDateStr + "/" + data[1])
                        .setFileName(data[1])
                        .setFileDumpDate(dumpFileDateStr)
                        .build());
            }
        }
        return response;
    }
    private String parseWikiDateFromFile(String wikiFileName) {
        return StringUtils.substringBetween(wikiFileName, "enwiki-", "-pages");
    }

    private Collection<String[]> parseFileList(String m) {
        Collection<String[]> fileList = parseFileList(m, WIKI_FILE_TYPE.MULTISTREAM);
        if (CollectionUtils.isEmpty(fileList)) {
            fileList = parseFileList(m, WIKI_FILE_TYPE.ARTICLE);
        }
        if (CollectionUtils.isEmpty(fileList)) {
            log.error("no valid files found.  Exiting.");
            System.exit(-1);
        }
        return fileList;
    }

    private Collection<String[]> parseFileList(String m, WIKI_FILE_TYPE type) {
        Collection<String[]> filesToDownload = new ArrayList<>();
        String[] lines = m.split("\n");
        String[] data;
        for (String line:lines) {
            data = line.split(" {2}");
            String fileName = data[1];
            if (isFileType(fileName, type)) {
                filesToDownload.add(data);
            }
        }
        return filesToDownload;
    }

    private boolean isFileType(String fileName, WIKI_FILE_TYPE fileType) {
        if (fileType == WIKI_FILE_TYPE.MULTISTREAM) {
            return isMultiStreamArticleFile(fileName);
        } else if (fileType == WIKI_FILE_TYPE.ARTICLE) {
            return isArticleFile(fileName);
        } else {
            return false;
        }
    }

    private static boolean isArticleFile(String fileName) {
        return fileName.contains("pages-article") &&
                !fileName.contains("pages-articles.xml.bz2");
    }


    private enum WIKI_FILE_TYPE {
        MULTISTREAM, ARTICLE
    }

    private static boolean isMultiStreamArticleFile(String fileName) {
        return fileName.contains("pages-articles-multistream") &&
                !fileName.contains("pages-articles-multistream.xml.bz2") &&
                !fileName.contains("index");
    }


}