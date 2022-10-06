package com.krickert.util.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class AsyncDownloaderRunnable implements Runnable {

    final private URL urlToDownload;
    final private String destFile;
    final private String md5;
    final private Integer maxTries;
    private final Logger logger = LoggerFactory.getLogger(AsyncDownloaderRunnable.class);

    public AsyncDownloaderRunnable(final URL urlToDownload, final String destFile, final String md5, final Integer maxTries) {
        this.urlToDownload = urlToDownload;
        this.destFile = destFile;
        this.md5 = md5;
        this.maxTries = maxTries;
    }

    @Override
    public void run() {
        logger.info("Checking to see if file exists and if it matches the md5");
        File downloadedFile = new File(destFile);
        if (Files.exists(downloadedFile.toPath())) {
            logger.info("Checking the md5 of already existing file");
            String currentMd5;
            try {
                currentMd5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(downloadedFile));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (md5.equals(currentMd5)) {
                logger.info("File already downloaded and matches md5.  Skipping");
                return;
            }
        }
        logger.info("URL: " + urlToDownload.toString() + " DOWNLOADING " + destFile);
        Downloader downloader = new Downloader(maxTries);
        downloader.download(urlToDownload, downloadedFile, md5);
    }
}
