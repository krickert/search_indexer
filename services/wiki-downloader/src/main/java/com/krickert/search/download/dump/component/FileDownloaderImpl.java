package com.krickert.search.download.dump.component;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

@Singleton
@Requires(notEnv = Environment.TEST)
public class FileDownloaderImpl implements FileDownloader {

    private static final Logger log = LoggerFactory.getLogger(FileDownloaderImpl.class);

    private final Integer maxTries;

    private final ConcurrentMap<String, DownloadStatus> globalStatus = Maps.newConcurrentMap();

    @Inject
    public FileDownloaderImpl(@Value("${download.max-tries}") Integer maxTries) {
        this.maxTries = maxTries;
    }

    private static void createBackupOfFile(File dstFile, String md5) {
        File backupFile = getBackupFile(dstFile);
        try {
            FileUtils.copyFile(dstFile, backupFile);
            log.info("backed up file {} as {}", dstFile.getName(), backupFile.getName());
        } catch (IOException e) {
            log.error("we couldn't copy the backup file {} from existing file {} due to mismatch md5 {}",
                    backupFile.getName(), dstFile.getName(), md5);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static File getBackupFile(File dstFile) {
        File backupFile = new File(dstFile.getName() + ".backup");
        if (backupFile.exists()) {
            return getBackupFile(backupFile);
        }
        return backupFile;
    }

    @Override
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
            File returnVal = downloadFromClient(url, dstFile, httpclient);
            globalStatus.put(dstFile.getName(), DownloadStatus.COMPLETED);
            return returnVal;
        } catch (Exception e) {
            globalStatus.put(dstFile.getName(), DownloadStatus.ERROR);
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(httpclient);
        }
    }

    public File downloadFromClient(URL url, File dstFile, CloseableHttpClient httpclient) throws IOException, URISyntaxException {
        return httpclient.execute(new HttpGet(url.toURI()), new FileDownloadResponseHandler(dstFile));
    }

    @Override
    public void download(URL url, File dstFile, String md5) {
        boolean downloadSuccess = false;
        File downloaded = null;
        if (dstFile.isDirectory()) {
            throw new IllegalArgumentException("file " + dstFile.getName() + " is a directory.");
        }
        if (dstFile.exists() && dstFile.isFile()) {
            if (checkMd5(md5, dstFile)) {
                log.info("File {} has md5 {} and is already there.  Skipping.", dstFile.getAbsoluteFile(), md5);
                return;
            } else {
                log.error("File {} exists and does not match the MD5 {}.  Renaming file and downloading new one",
                        dstFile.getName(), md5);
                createBackupOfFile(dstFile, md5);
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

    @Override
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

    public enum DownloadStatus {
        IN_PROGRESS, COMPLETED, ERROR
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

}
