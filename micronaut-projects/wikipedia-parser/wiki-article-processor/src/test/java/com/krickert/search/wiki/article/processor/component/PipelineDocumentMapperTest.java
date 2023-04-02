package com.krickert.search.wiki.article.processor.component;

import com.google.common.collect.Lists;
import com.google.protobuf.Timestamp;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static com.krickert.search.model.test.util.TestDataHelper.getFewHunderedArticles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineDocumentMapperTest {
    private static final Logger log = LoggerFactory.getLogger(PipelineDocumentMapperTest.class);
    PipelineDocumentMapper mock = new PipelineDocumentMapper();

    @Test
    void testDateConversion() {
        Timestamp now = Timestamp.newBuilder().setSeconds(1668046556).setNanos(204003000).build();
        String date = mock.parseDateParsed(now);
        log.info("Date answer: {} seconds: {} nanos: {}", date, now.getSeconds(), now.getNanos());
        assertEquals("2022-11-10T02:15:56.204003Z", date);
    }

    @Test
    void mapWikiArticleToPipeDocument() {
        Collection<WikiArticle> articles = getFewHunderedArticles();
        assertThat(articles).isNotNull()
                .hasSize(367);
        Collection<PipeDocument> mapperDocuments = Lists.newArrayList();
        PipelineDocumentMapper mapper = new PipelineDocumentMapper();
        articles.forEach((article) -> mapperDocuments.add(mapper.mapWikiArticleToPipeDocument(article)));
        assertThat(mapperDocuments).isNotEmpty()
                .hasSize(367);
        mapperDocuments.forEach((pipeDocument) ->
                assertThat(pipeDocument).isNotNull()
                        .isInstanceOf(PipeDocument.class));
        Collection<String> articleIds = Lists.newArrayList();
        articles.forEach((article -> articleIds.add(article.getId())));
        Collection<String> pipeIds = Lists.newArrayList();
        mapperDocuments.forEach((document -> pipeIds.add(document.getId())));
        assertThat(articleIds).containsAll(pipeIds);
    }


}