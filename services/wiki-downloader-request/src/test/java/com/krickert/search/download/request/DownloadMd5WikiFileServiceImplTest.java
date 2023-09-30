package com.krickert.search.download.request;

import com.krickert.search.download.request.component.DownloadMd5WikiFileServiceImpl;
import com.krickert.search.download.request.util.MicronautFileUtil;
import io.micronaut.rxjava3.http.client.Rx3HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@MicronautTest
class DownloadMd5WikiFileServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(DownloadMd5WikiFileServiceImplTest.class);
    public static final String WIKI_LIST_MD_5 = "wikiList.md5";
    public static final String WIKI_LIST_MD_5_TEXT = MicronautFileUtil.readFileAsString(WIKI_LIST_MD_5).get();
    public static final String WIKI_LIST_NO_MULTISTREAM_MD_5 = "wikiListNoMultistream.md5";
    private static final Collection<File> filesToDelete = new ArrayList<>();
    private final DownloadMd5WikiFileServiceImpl mock;
    private final DownloadMd5WikiFileServiceImpl mock2;
    @Inject
    DownloadMd5WikiFileServiceImplTest(Rx3HttpClient client) {
        DownloadMd5WikiFileServiceImpl service = new DownloadMd5WikiFileServiceImpl(WIKI_LIST_MD_5, false, client);
        DownloadMd5WikiFileServiceImpl spied = Mockito.spy(service);
        Optional<String> md5Str = MicronautFileUtil.readFileAsString(WIKI_LIST_MD_5);
        if (md5Str.isEmpty()) {
            fail(WIKI_LIST_MD_5 + " file is not there");
        }
        Mockito.doReturn(WIKI_LIST_MD_5_TEXT).when(spied).retrieveWikiDumpFileContentsFromWikipedia();
        this.mock = spied;
        DownloadMd5WikiFileServiceImpl service2 = new DownloadMd5WikiFileServiceImpl(null, false, client);
        DownloadMd5WikiFileServiceImpl spied2 = Mockito.spy(service2);
        Mockito.doReturn(MicronautFileUtil.readFileAsString(WIKI_LIST_NO_MULTISTREAM_MD_5).get()).when(spied2).retrieveWikiDumpFileContentsFromWikipedia();
        this.mock2 = spied2;
    }

    @AfterAll
    public static void deleteFiles() {
        filesToDelete.forEach((file) -> {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.warn("file didn't delete right - probably because this is running on windows", e);
            }
        });
    }

    @Test
    void downloadTestMockingString() {
        assertThat(
                mock.retrieveWikiDumpFileContentsFromWikipedia())
                .isEqualTo(MicronautFileUtil.readFileAsString(WIKI_LIST_MD_5).get()
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
    void downloadWhenFileNotThere() {
        String result = mock.downloadWikiMd5AsString("wikiListNotThere.md5");
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