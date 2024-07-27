package com.krickert.search.indexer.enhancers;

import java.util.List;

public interface Chunker {
    public List<String> chunk(String text);
}
