package com.krickert.search.download.request;


import com.krickert.search.model.wiki.DownloadFileRequest;

import java.util.Collection;

public interface DownloadMd5WikiFileService {
    String downloadWikiMd5AsString(String fileList);

    Collection<String[]> parseFileList(String m);

    Collection<String[]> parseFileList(String m, DownloadMd5WikiFileServiceImpl.WIKI_FILE_TYPE type);

    Collection<DownloadFileRequest> createDownloadRequests(Collection<String[]> fileList);
}
