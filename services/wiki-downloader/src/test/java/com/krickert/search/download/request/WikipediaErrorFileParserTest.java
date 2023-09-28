package com.krickert.search.download.request;

import com.krickert.search.download.request.component.WikipediaErrorFileParser;
import com.krickert.search.download.request.util.MicronautFileUtil;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@MicronautTest
class WikipediaErrorFileParserTest {
    public static final String WIKI_LIST_SMALL_NO_MULTISTREAM_MD_5 = "wikiListSmallNoMultistream.md5";
    public static final String WIKI_LIST_SMALL = "wikiListSmall";
    public static final String ENWIKI_20221020_PAGES_ARTICLES_1 = "enwiki-20221020-pages-articles1.";
    public static final String ENWIKI_20221020_PAGES_ARTICLES_2 = "enwiki-20221020-pages-articles2";
    public static final String DUMP_FILE_DATE = "20221020";
    static final String fileDumpDate = DUMP_FILE_DATE;
    @Inject
    WikipediaErrorFileParser mock;
    String fileWithNoMultistream;
    String fullDumpFile;
    String[] article1 = {"c0fb90f060d7b95c06931eb852a3e920", ENWIKI_20221020_PAGES_ARTICLES_1 +
            "xml-p1p41242.bz2"};
    String[] article2 = {"79ff3aa9ffc57c2b58533af27dd78dbd", ENWIKI_20221020_PAGES_ARTICLES_2 +
            ".xml-p41243p151573.bz2"};
    Collection<String[]> articles = Arrays.asList(article1, article2);
    DownloadFileRequest request1 = DownloadFileRequest.newBuilder()
            .setFileName(article1[1])
            .setFileDumpDate(fileDumpDate)
            .setUrl(createTestUrl(article1[1]))
            .setErrorCheck(
                    ErrorCheck.newBuilder()
                            .setErrorCheck(article1[0])
                            .setErrorCheckType(ErrorCheckType.MD5)
                            .build())
            .build();
    DownloadFileRequest request2 = DownloadFileRequest.newBuilder()
            .setFileName(article2[1])
            .setFileDumpDate(fileDumpDate)
            .setUrl(createTestUrl(article2[1]))
            .setErrorCheck(
                    ErrorCheck.newBuilder()
                            .setErrorCheck(article2[0])
                            .setErrorCheckType(ErrorCheckType.MD5)
                            .build())
            .build();


    String[] multiArticle1 = {"d7b7d62e9f6bf7051f0a253c5f93b12a", "enwiki-20221020-pages-articles-multistream1.xml-p1p41242.bz2"};
    String[] multiArticle2 = {"950c2417d7ff37eb0c8c82e6656460ec", "enwiki-20221020-pages-articles-multistream2.xml-p41243p151573.bz2"};
    String[] multiArticle3 = {"ff2f0f432dce02d00676b5bd06008748", "enwiki-20221020-pages-articles-multistream3.xml-p151574p311329.bz2"};
    Collection<String[]> multiArticles = Arrays.asList(multiArticle1, multiArticle2, multiArticle3);
    DownloadFileRequest multiRequest1 = DownloadFileRequest.newBuilder()
            .setFileName(multiArticle1[1])
            .setFileDumpDate(fileDumpDate)
            .setUrl(createTestUrl(multiArticle1[1]))
            .setErrorCheck(
                    ErrorCheck.newBuilder()
                            .setErrorCheck(multiArticle1[0])
                            .setErrorCheckType(ErrorCheckType.MD5)
                            .build())
            .build();
    DownloadFileRequest multiRequest2 = DownloadFileRequest.newBuilder()
            .setFileName(multiArticle2[1])
            .setFileDumpDate(fileDumpDate)
            .setUrl(createTestUrl(multiArticle2[1]))
            .setErrorCheck(
                    ErrorCheck.newBuilder()
                            .setErrorCheck(multiArticle2[0])
                            .setErrorCheckType(ErrorCheckType.MD5)
                            .build())
            .build();
    DownloadFileRequest multiRequest3 = DownloadFileRequest.newBuilder()
            .setFileName(multiArticle3[1])
            .setFileDumpDate(fileDumpDate)
            .setUrl(createTestUrl(multiArticle3[1]))
            .setErrorCheck(
                    ErrorCheck.newBuilder()
                            .setErrorCheck(multiArticle3[0])
                            .setErrorCheckType(ErrorCheckType.MD5)
                            .build())
            .build();

    private static String createTestUrl(String fileName) {
        //"https://dumps.wikimedia.org/enwiki/20221020/enwiki-20221020-pages-articles1.xml-p1p41242.bz2"
        return "https://dumps.wikimedia.org/enwiki/" + fileDumpDate + "/" + fileName;
    }

    @BeforeEach
    void setUp() {
        Optional<String> fileWithNoMultistream = MicronautFileUtil.readFileAsString(WIKI_LIST_SMALL_NO_MULTISTREAM_MD_5);
        Optional<String> fullDumpFile = MicronautFileUtil.readFileAsString(WIKI_LIST_SMALL +
                ".md5");
        if (fileWithNoMultistream.isPresent()) {
            this.fileWithNoMultistream = fileWithNoMultistream.get();
        } else {
            fail("file with no multistream test not present");
        }
        if (fullDumpFile.isPresent()) {
            this.fullDumpFile = fullDumpFile.get();
        } else {
            fail("full dump file is not present");
        }
    }

    @Test
    void parseFileList() {
        Collection<String[]> values = mock.parseFileList(fullDumpFile);
        assertThat(values.size()).isEqualTo(3);
        assertThat(values).contains(multiArticle1, multiArticle2, multiArticle3);
    }

    @Test
    void parseFileListNoMultistream() {
        Collection<String[]> values = mock.parseFileList(fileWithNoMultistream);
        assertThat(values.size()).isEqualTo(2);
        assertThat(values).contains(article1, article2);
    }

    @Test
    void testParseFileList() {
        Collection<String[]> values = mock.parseFileList(fullDumpFile, WikipediaErrorFileParser.WIKI_FILE_TYPE.ARTICLE);
        assertThat(values.size()).isEqualTo(2);
        assertThat(values).contains(article1, article2);
    }

    @Test
    void testParseFileListMultistream() {
        Collection<String[]> values = mock.parseFileList(fullDumpFile, WikipediaErrorFileParser.WIKI_FILE_TYPE.MULTISTREAM);
        assertThat(values.size()).isEqualTo(3);
        assertThat(values).contains(multiArticle1, multiArticle2, multiArticle3);
    }

    @Test
    void testParseFileListNoMultistream() {
        Collection<String[]> values = mock.parseFileList(fileWithNoMultistream, WikipediaErrorFileParser.WIKI_FILE_TYPE.MULTISTREAM);
        assertThat(values.size()).isEqualTo(0);
    }

    @Test
    void createDownloadRequestsArticleType() {
        assertThat(mock.createDownloadRequests(articles))
                .hasSize(2)
                .contains(request1)
                .contains(request2);
    }

    @Test
    void createDownloadRequestsMultiArticleType() {
        assertThat(mock.createDownloadRequests(multiArticles))
                .hasSize(3)
                .contains(multiRequest1)
                .contains(multiRequest2)
                .contains(multiRequest3);
    }
}