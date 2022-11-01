package com.krickert.search.pipline.messaging;

import com.krickert.search.model.ParsedWikiArticle;
import com.krickert.search.model.PipelineDocument;

public class ParsedArticleToPipelineDocumentMapper implements PipelineDocumentMapper<ParsedWikiArticle> {

    @Override
    public PipelineDocument mapDocument(ParsedWikiArticle inputDoc) {
        return PipelineDocument.newBuilder().build();
    }
}
