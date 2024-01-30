package com.krickert.search.wiki.dump.file.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class WikiDumpFileProcessorTest {
    @Inject
    WikiDumpFileProcessor wikiDumpFileProcessor;

    @Test
    public void testProcess() throws InterruptedException {
        ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:enwiki-20221101-pages-articles2.xml-short.xml.bz2");
        assertThat(resource.isPresent()).isTrue();
        String fileName = resource.get().getFile();
        Timestamp start = ProtobufUtils.now();
        Thread.sleep(200);
        DownloadedFile downloadedFile = DownloadedFile.newBuilder().setDownloadStart(ProtobufUtils.now()).setFileDumpDate("20221101").setFileName(fileName).setFullFilePath(resource.get().getFile()).setErrorCheck(ErrorCheck.newBuilder().setErrorCheckType(ErrorCheckType.MD5).setErrorCheck("65dd15906450b503691577aa6d08df2b").build()).setServerName("localhost").setDownloadStart(start).setDownloadEnd(ProtobufUtils.now()).build();
        wikiDumpFileProcessor.process(downloadedFile);

    }
}
