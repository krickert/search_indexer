package com.krickert.search.download.request.component;

import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Singleton
public class WikipediaErrorFileParser {
    private static final Logger log = LoggerFactory.getLogger(WikipediaErrorFileParser.class);

    final String wikipediaPrefixUrl;

    public WikipediaErrorFileParser(
            @Value("${wikipedia.dump-url}")
            String wikipediaPrefixUrl) {
        this.wikipediaPrefixUrl = wikipediaPrefixUrl;
    }

    private static boolean isArticleFile(String fileName) {
        return !fileName.contains("multistream") &&
                fileName.contains("pages-article") &&
                !fileName.contains("pages-articles.xml.bz2") &&
                !fileName.contains("index");
    }

    private static boolean isMultiStreamArticleFile(String fileName) {
        return fileName.contains("pages-articles-multistream") &&
                !fileName.contains("pages-articles-multistream.xml.bz2") &&
                !fileName.contains("index");
    }

    public Collection<String[]> parseFileList(String m) {
        Collection<String[]> fileList = parseFileList(m, WIKI_FILE_TYPE.MULTISTREAM);
        if (CollectionUtils.isEmpty(fileList)) {
            fileList = parseFileList(m, WIKI_FILE_TYPE.ARTICLE);
        }
        if (CollectionUtils.isEmpty(fileList)) {
            log.warn("no valid files found.  latest dumps might not be ready");
            return Collections.emptyList();
        }
        return fileList;
    }

    public Collection<String[]> parseFileList(String m, WIKI_FILE_TYPE type) {
        Collection<String[]> filesToDownload = new ArrayList<>();
        String[] lines = m.split("\n");
        String[] data;
        for (String line : lines) {
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
            log.error("unsupported and impossible file type");
            return false;
        }
    }

    public Collection<DownloadFileRequest> createDownloadRequests(Collection<String[]> fileList) {
        Collection<DownloadFileRequest> response = new ArrayList<>(fileList.size());

        for (String[] data : fileList) {
            if (isValidData(data)) {
                response.add(createRequestForDownloadFile(data));
            }
        }
        return response;
    }

    private boolean isValidData(String[] data) {
        return ArrayUtils.isNotEmpty(data) && data.length == 2;
    }

    private DownloadFileRequest createRequestForDownloadFile(String[] data) {
        String dumpFileDateStr = parseWikiDateFromFile(data[1]);

        ErrorCheck errorCheck = ErrorCheck.newBuilder()
                .setErrorCheck(data[0])
                .setErrorCheckType(ErrorCheckType.MD5).build();

        String url = wikipediaPrefixUrl + "/" + dumpFileDateStr + "/" + data[1];

        return DownloadFileRequest.newBuilder()
                .setErrorCheck(errorCheck)
                .setUrl(url)
                .setFileName(data[1])
                .setFileDumpDate(dumpFileDateStr)
                .build();
    }

    private String parseWikiDateFromFile(String wikiFileName) {
        return StringUtils.substringBetween(wikiFileName, "enwiki-", "-pages");
    }

    public enum WIKI_FILE_TYPE {
        MULTISTREAM, ARTICLE
    }

}
