package com.krickert.search.download.dump.component;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;

@MicronautTest
class FileDownloaderImplTest {


    FileDownloaderImpl fileDownloader = createMockDownloader();

    FileDownloaderImplTest() throws IOException, URISyntaxException {
    }

    private FileDownloaderImpl createMockDownloader() throws IOException, URISyntaxException {
        FileDownloaderImpl fileDownloader = new FileDownloaderImpl(5);
        FileDownloaderImpl spied = spy(fileDownloader);
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        Mockito.doReturn(new File(resource.get().getFile())).when(spied).downloadFromClient(any(), any(), any());
        return spied;
    }

    @Test
    void downloadWithMd5FileIsThere() throws MalformedURLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        fileDownloader.download(new URL("http://www.example.com"), new File(resource.get().getFile()), "00a5f9e5b648a2dcc1f90eb692ec7115");
        assertThat(baos.toString()).contains("and is already there");
    }

    @Test
    void downloadWithMd5() throws MalformedURLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        fileDownloader.download(new URL("http://www.example.com"), new File("notTheFileBecauseMock.txt"), "00a5f9e5b648a2dcc1f90eb692ec7115");
        assertThat(baos.toString()).contains("file was downloaded successfully");
    }

    @Test
    void testDownload() throws MalformedURLException {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        File expected = new File(resource.get().getFile());
        File returned = fileDownloader.download(new URL("http://www.example.com"), new File("notTheFileBecauseMock.txt"));
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    void checkMd5() {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        assertThat(fileDownloader.checkMd5("00a5f9e5b648a2dcc1f90eb692ec7115", new File(resource.get().getFile()))).isTrue();
    }

    @Test
    void checkMd5FailWhenFileNotThere() {
        assertThat(fileDownloader.checkMd5("00a5f9e5b648a2dcc1f90eb692ec7115", new File("nothing to see here"))).isFalse();
    }

    @Test
    void checkMd5FailWhenFileNull() {
        assertThat(fileDownloader.checkMd5("00a5f9e5b648a2dcc1f90eb692ec7115", null)).isFalse();
    }

    @Test
    void checkMd5FailWhenMd5AndFileNull() {
        assertThat(fileDownloader.checkMd5(null, null)).isFalse();
    }

    @Test
    void checkMd5FailWhenMd5Wrong() {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:fakeDownloadedFile.txt");
        assertThat(fileDownloader.checkMd5("0aa5f9e5b648a2dcc1f90eb692ec7115", new File(resource.get().getFile()))).isFalse();
    }
}