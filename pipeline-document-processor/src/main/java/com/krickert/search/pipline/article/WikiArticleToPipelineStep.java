package com.krickert.search.pipline.article;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;
import jakarta.inject.Singleton;

@Singleton
public class WikiArticleToPipelineStep implements Step<WikiArticle, PipeDocument> {


    @Override
    public PipeDocument execute(WikiArticle value) {
        return PipeDocument.newBuilder().setId("555").setTitle(value.getTitle()).setBody("blah").build();
    }

    @Override
    public <R> Step<WikiArticle, R> pipe(Step<PipeDocument, R> source) {
        return Step.super.pipe(source);
    }

    public static void main(String args[]) {
        WikiArticleToPipelineStep step = new WikiArticleToPipelineStep();
        PipeDocument doc = step.execute(WikiArticle.newBuilder().setId("333").setTitle("Copied title.").setText("blahblah").setWikiText("blahblah2").build());
        System.out.println(doc);

    }
}
