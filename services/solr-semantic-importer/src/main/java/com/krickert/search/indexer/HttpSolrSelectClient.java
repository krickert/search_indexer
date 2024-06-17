package com.krickert.search.indexer;

import java.io.IOException;

public interface HttpSolrSelectClient {
    String getSolrDocs(Integer paginationSize, Integer pageNumber) throws IOException, InterruptedException;
}
