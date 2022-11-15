package com.krickert.search.wiki.article.processor.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class PipelineDocumentMapper {

    public PipeDocument mapWikiArticleToPipeDocument(WikiArticle wikiArticle) {
        PipeDocument.Builder pipeDocumentBuilder = PipeDocument.newBuilder();
        pipeDocumentBuilder.setId(wikiArticle.getId())
                .setRevisionId(wikiArticle.getRevisionId())
                .setTitle(wikiArticle.getTitle())
                .setDocumentType(wikiArticle.getWikiType().name())
                .setCreationDate(wikiArticle.getTimestamp());
        pipeDocumentBuilder.putCustom("namespace", wikiArticle.getNamespace())
                .putCustom("dump_date", wikiArticle.getDumpTimestamp());
        if (isNotEmpty(wikiArticle.getText())) {
            pipeDocumentBuilder.setBody(wikiArticle.getText());
        }
        if (isNotEmpty(wikiArticle.getWikiText())) {
            pipeDocumentBuilder.putCustom("wiki_text", wikiArticle.getWikiText());
        }
        pipeDocumentBuilder.putCustom("date_parsed", parseDateParsed(wikiArticle.getDateParsed()));
        return pipeDocumentBuilder.build();
    }

    String parseDateParsed(Timestamp dateParsed) {
        Instant date = Instant.ofEpochSecond(dateParsed.getSeconds(), dateParsed.getNanos());
        DateTimeFormatter formatter = ISO_INSTANT.withZone(ZoneId.from(ZoneOffset.UTC));
        return formatter.format(date);
    }

}
