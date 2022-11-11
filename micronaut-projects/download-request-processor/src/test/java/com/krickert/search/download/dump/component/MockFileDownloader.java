package com.krickert.search.download.dump.component;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import jakarta.inject.Singleton;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Singleton
public class MockFileDownloader implements FileDownloader {
    @Override
    public File download(URL url, File dstFile) {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        try {
            FileUtils.copyFile(new File(resource.get().getFile()), dstFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dstFile;
    }

    @Override
    public void download(URL url, File dstFile, String md5) {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        try {
            FileUtils.copyFile(new File(resource.get().getFile()), dstFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkMd5(String md5Sum, File theFile) {
        return true;
    }
}
