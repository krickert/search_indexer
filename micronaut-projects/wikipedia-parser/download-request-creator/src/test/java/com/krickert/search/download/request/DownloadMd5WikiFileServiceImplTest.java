package com.krickert.search.download.request;

import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import static io.micronaut.http.HttpRequest.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@MicronautTest
class DownloadMd5WikiFileServiceImplTest {

    private static Collection<File> filesToDelete = new ArrayList<>();

    @AfterClass
    void deleteFiles() {
        filesToDelete.forEach((file) -> {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
            }
        });
    }

    private final DownloadMd5WikiFileServiceImpl mock;
    private final DownloadMd5WikiFileServiceImpl mock2;

    @Inject
    DownloadMd5WikiFileServiceImplTest(Rx3HttpClient client) {
        DownloadMd5WikiFileServiceImpl service = new DownloadMd5WikiFileServiceImpl("wikiList.md5", false, client);
        DownloadMd5WikiFileServiceImpl spied = Mockito.spy(service);
        Mockito.doReturn(MicronautFileUtil.readFileAsString("wikiList.md5").get()).when(spied).retrieveWikiDumpFileContentsFromWikipedia();
        this.mock = spied;
        DownloadMd5WikiFileServiceImpl service2 = new DownloadMd5WikiFileServiceImpl(null, false, client);
        DownloadMd5WikiFileServiceImpl spied2 = Mockito.spy(service2);
        Mockito.doReturn(MicronautFileUtil.readFileAsString("wikiListNoMultistream.md5").get()).when(spied2).retrieveWikiDumpFileContentsFromWikipedia();
        this.mock2 = spied2;
    }


    @Test
    void downloadTestMockingString() {
        assertThat(
                mock.retrieveWikiDumpFileContentsFromWikipedia())
                .isEqualTo(MicronautFileUtil.readFileAsString("wikiList.md5").get()
        );
    }

    @Test
    void readArticleTypes() {
        assertThat(
                mock.downloadWikiMd5AsString("wikiListNoMultistream.md5"))
                .isEqualTo(MicronautFileUtil.readFileAsString("wikiListNoMultistream.md5").get()
                );
    }

    @Test
    void downloadWhenFileNotThere() throws IOException {
        String result =  mock.downloadWikiMd5AsString("wikiListNotThere.md5");
        assertThat(result)
                .isEqualTo(MicronautFileUtil.readFileAsString("wikiListNotThere.md5").get()
                );
        filesToDelete.add(new File("wikiListNotThere.md5"));
    }

    @Test
    void downloadReadNoArticleFileFromProperties() {
        assertThat(
                mock2.retrieveWikiDumpFileContentsFromWikipedia())
                .isEqualTo(MicronautFileUtil.readFileAsString("wikiListNoMultistream.md5").get()
                );
    }
    @Test
    void downloadFromInternetWhenNoParametersAreThere() throws IOException {
        String contents = mock2.downloadWikiMd5AsString(null);
        assertThat(contents)
                .isEqualTo(MicronautFileUtil.readFileAsString("wikiListNoMultistream.md5").get()
                );
        //a file should've been created
        File file = new File("wikiList.md5");
        assertThat(file.exists()).isTrue();
        assertThat(FileUtils.readFileToString(file, Charset.defaultCharset())).isEqualTo(contents);
        FileUtils.forceDelete(file);
    }
}