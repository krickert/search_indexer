package com.krickert.util.download;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Component
public class Downloader {
    private final Logger logger = LoggerFactory.getLogger(Downloader.class);
    private final Integer maxTries;

    public Downloader(final @Value("${download.maxTries}") Integer maxTries) {
        this.maxTries = maxTries;
    }

    public File download(URL url, File dstFile) {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                .build();
        try {
            HttpGet get = new HttpGet(url.toURI()); // we're using GET but it could be via POST as well
            return httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(httpclient);
        }
    }

    public void download(URL url, File dstFile, String md5) {
        boolean downloadSuccess = false;
        File downloaded = null;
        for (int numTries = 0; numTries < maxTries && !downloadSuccess; numTries++) {
            try {
                downloaded = download(url, dstFile);
            } catch (IllegalStateException e) {
                downloaded = null;
            }
            downloadSuccess = checkMd5(md5, downloaded);
            if (!downloadSuccess) {
                try {
                    logger.info("Since there was an error in downloading {}, sleeping for 5 seconds before starting again.", url.toExternalForm());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (downloaded == null) {
            logger.error("Download didn't happen after multiple attempts {}", url.toExternalForm());
        }
        if (!downloadSuccess) {
            logger.error("md5 checksums did not match after multiple attempts to download " + url.toExternalForm());
        }
    }

    public boolean checkMd5(String md5Sum, File theFile) {
        try {
            if (theFile == null || !theFile.exists()) {
                logger.error("file is null or does not exist.  md5 cannot match");
                return false;
            }
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(new FileInputStream(theFile));
            if (md5Sum.equals(md5)) {
                logger.info("file was downloaded successfully");
                return true;
            } else {
                logger.info("md5 does not match.  Download filed for " + theFile.getAbsolutePath());
                return false;
            }
        } catch (IOException e) {
            logger.error("IOException happened", e);
            return false;
        }
    }

    static class FileDownloadResponseHandler implements ResponseHandler<File> {

        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(HttpResponse response) throws IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }

    }


}
