package com.krickert.search.service.vectorizer;

import com.krickert.search.vectorizer.SentenceVectorizer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class SentenceVectorizerTest {
    @Inject
    SentenceVectorizer vectorizer;

    @Test
    void testVetorizerTestReturns384Dimensions() {
        Assertions.assertNotNull(vectorizer);
        Assertions.assertEquals(384, vectorizer.embeddings("Vectorize 4 lyfe").length);
    }

    @Test
    void testVetorizerTestReturns384DimensionsList() {
        Assertions.assertNotNull(vectorizer);
        Assertions.assertEquals(384, vectorizer.getEmbeddings("Vectorize 4 lyfe").size());
    }

}
