package com.krickert.search.download.request;


import com.krickert.search.download.request.component.DownloadMd5WikiFileService;
import com.krickert.search.download.request.util.MicronautFileUtil;
import jakarta.inject.Singleton;

@Singleton
public class MockDownloadMd5WikiFileService implements DownloadMd5WikiFileService {

    @Override
    public String downloadWikiMd5AsString(String fileList) {
        return MicronautFileUtil.readFileAsString("wikiList.md5").get();
    }
}
