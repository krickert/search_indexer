package com.krickert.search.pipline.article;

import com.krickert.search.model.ParsedWikiArticle;
import com.krickert.search.model.PipelineDocument;
import jakarta.inject.Singleton;

@Singleton
public class WikiArticleToPipelineStep implements Step<ParsedWikiArticle, PipelineDocument> {


    @Override
    public PipelineDocument execute(ParsedWikiArticle value) {
        return PipelineDocument.newBuilder().setId("555").setTitle(value.getTitle()).setBody("blah").build();
    }

    @Override
    public <R> Step<ParsedWikiArticle, R> pipe(Step<PipelineDocument, R> source) {
        return Step.super.pipe(source);
    }

    public static void main(String args[]) {
        WikiArticleToPipelineStep step = new WikiArticleToPipelineStep();
        PipelineDocument doc = step.execute(ParsedWikiArticle.newBuilder().setId("333").setTitle("Copied title.").setText("blahblah").setWikiText("blahblah2").build());
        System.out.println(doc);

    }
}
