package com.krickert.search.download.dump.component;

import java.io.File;
import java.net.URL;

public interface FileDownloader {
    File download(URL url, File dstFile);

    void download(URL url, File dstFile, String md5);

    boolean checkMd5(String md5Sum, File theFile);
}
