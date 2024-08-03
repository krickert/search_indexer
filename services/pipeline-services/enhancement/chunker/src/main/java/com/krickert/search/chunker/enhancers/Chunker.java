package com.krickert.search.chunker.enhancers;

import java.util.List;

public interface Chunker {
    public List<String> chunk(String text);
}
