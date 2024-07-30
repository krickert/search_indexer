package com.krickert.search.vectorizer;

import java.util.Collection;

public interface Vectorizer {
    float[] embeddings(String text);

    Collection<Float> getEmbeddings(String text);
}
