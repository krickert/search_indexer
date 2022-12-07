package com.krickert.search.model.test.util;

import com.krickert.search.model.wiki.WikiArticle;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDataHelperTest {

    @Test
    void testGetFewHunderedArticles() {
        Collection<WikiArticle> result = TestDataHelper.getFewHunderedArticles();
        assertThat(result)
                .isNotNull()
                .hasSize(367);
        result.forEach((article) -> assertThat(article)
                .isNotNull()
                .isInstanceOf(WikiArticle.class)
        );
    }
}
