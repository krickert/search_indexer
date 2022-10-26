package com.krickert.search.download.dump.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

@Slf4j
@Singleton
public class FileDownloader {
    private final Integer maxTries;

    private final ConcurrentMap<String, DownloadStatus> globalStatus = Maps.newConcurrentMap();

    public FileDownloader(@Value("${download.max-tries}") Integer maxTries) {
        this.maxTries = maxTries;
    }

    public File download(URL url, File dstFile) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(dstFile);
        DownloadStatus status = globalStatus.get(dstFile.getName());
        if (status == DownloadStatus.IN_PROGRESS) {
            log.warn("duplicate download is in progress.  not downloading {} from {}", dstFile.getName(), url.toExternalForm());
            throw new IllegalStateException("Duplicate download requested. Not downloading.");
        }
        globalStatus.put(dstFile.getName(), DownloadStatus.IN_PROGRESS);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                .build();

        try {
            ClassicHttpRequest get = new HttpGet(url.toURI()); // we're using GET but it could be via POST as well
            globalStatus.put(dstFile.getName(), DownloadStatus.COMPLETED);
            return httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
        } catch (Exception e) {
            globalStatus.put(dstFile.getName(), DownloadStatus.ERROR);
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(httpclient);
        }
    }

    public void download(URL url, File dstFile, String md5) {
        boolean downloadSuccess = false;
        File downloaded = null;
        if (dstFile.isDirectory()) {
            throw new RuntimeException("file " + dstFile.getName() + " is a directory.");
        }
        if (dstFile.exists() && dstFile.isFile()) {
            if(checkMd5(md5,dstFile)) {
                log.info("File {} has md5 {} and is already there.  Skipping.", dstFile.getAbsoluteFile(), md5);
                return;
            }
        }
        for (int numTries = 0; numTries < maxTries && !downloadSuccess; numTries++) {
            try {
                downloaded = download(url, dstFile);
            } catch (IllegalStateException e) {
                downloaded = null;
            }
            downloadSuccess = checkMd5(md5, downloaded);
            if (!downloadSuccess) {
                try {
                    log.info("Since there was an error in downloading {}, sleeping for 5 seconds before starting again.", url.toExternalForm());
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (downloaded == null) {
            log.error("Download didn't happen after multiple attempts {}", url.toExternalForm());
        }
        if (!downloadSuccess) {
            log.error("md5 checksums did not match after multiple attempts to download " + url.toExternalForm());
        }
    }

    public boolean checkMd5(String md5Sum, File theFile) {
        try {
            if (theFile == null || !theFile.exists()) {
                log.error("file is null or does not exist.  md5 cannot match");
                return false;
            }
            String md5 = md5Hex(new FileInputStream(theFile));
            if (md5Sum.equals(md5)) {
                log.info("file was downloaded successfully");
                return true;
            } else {
                log.info("md5 does not match for {}", theFile.getAbsolutePath());
                return false;
            }
        } catch (IOException e) {
            log.error("IOException happened", e);
            return false;
        }
    }

    static class FileDownloadResponseHandler implements HttpClientResponseHandler<File> {

        private final File target;

        public FileDownloadResponseHandler(File target) {
            this.target = target;
        }

        @Override
        public File handleResponse(ClassicHttpResponse response) throws IOException {
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, this.target);
            return this.target;
        }
    }

    public enum DownloadStatus {
        IN_PROGRESS, COMPLETED, ERROR
    }

}
