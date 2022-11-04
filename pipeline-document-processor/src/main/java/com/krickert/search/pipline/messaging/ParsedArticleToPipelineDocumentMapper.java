package com.krickert.search.pipline.messaging;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;

public class ParsedArticleToPipelineDocumentMapper implements PipelineDocumentMapper<WikiArticle> {

    @Override
    public PipeDocument mapDocument(WikiArticle inputDoc) {
        return PipeDocument.newBuilder().build();
    }

}
