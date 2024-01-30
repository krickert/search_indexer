package com.krickert.search.wiki.article.processor.component;

import com.google.common.collect.Maps;
import com.google.protobuf.Struct;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.krickert.search.wiki.article.processor.component.ParagraphParser.splitIntoParagraphsAndRemoveEmpty;
import static com.krickert.search.wiki.article.processor.component.ParagraphParser.splitIntoSections;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Singleton
public class PipelineDocumentMapper {

    private final boolean isAddWikiText;
    private final ParagraphParser.ParagraphStrategy paragraphStrategy;

    public PipelineDocumentMapper(@io.micronaut.context.annotation.Value("${wikipedia.add-wiki-text}") boolean isAddWikiText,
                                  @io.micronaut.context.annotation.Value("${wikipedia.paragraph-strategy}")ParagraphParser.ParagraphStrategy paragraphStrategy) {
        this.isAddWikiText = isAddWikiText;
        this.paragraphStrategy = paragraphStrategy;
    }

    public PipeDocument mapWikiArticleToPipeDocument(WikiArticle wikiArticle) {
        PipeDocument.Builder pipeDocumentBuilder = PipeDocument.newBuilder();
        pipeDocumentBuilder.setId(wikiArticle.getId())
                .setRevisionId(wikiArticle.getRevisionId())
                .setTitle(wikiArticle.getTitle())
                .setDocumentType(wikiArticle.getWikiType().name())
                .setCreationDate(wikiArticle.getTimestamp());
        Map<String, Value> wikiMetaData = Maps.newHashMap();
        wikiMetaData.put("namespace", Value.newBuilder().setStringValue(wikiArticle.getNamespace()).build());
        wikiMetaData.put("dump_date", Value.newBuilder().setStringValue(wikiArticle.getDumpTimestamp()).build());
        wikiMetaData.put("date_parsed", Value.newBuilder().setStringValue(parseDateParsed(wikiArticle.getDateParsed())).build());
        if (isNotEmpty(wikiArticle.getText())) {
            pipeDocumentBuilder.setBody(wikiArticle.getText());
            if (paragraphStrategy == ParagraphParser.ParagraphStrategy.PARAGRAPH) {
                pipeDocumentBuilder.addAllBodyParagraphs(splitIntoParagraphsAndRemoveEmpty(wikiArticle.getText()));
            } else if (paragraphStrategy == ParagraphParser.ParagraphStrategy.SECTION) {
                pipeDocumentBuilder.addAllBodyParagraphs(splitIntoSections(wikiArticle.getText(),wikiArticle.getTitle()));
            }
        }

        if (isAddWikiText && isNotEmpty(wikiArticle.getWikiText())) {
            wikiMetaData.put("wiki_text", Value.newBuilder().setStringValue(wikiArticle.getWikiText()).build());
        }
        Struct wikiData = Struct.newBuilder().putAllFields(wikiMetaData).build();
        Value wikiDataValue = Value.newBuilder().setStructValue(wikiData).build();
        Struct customData = pipeDocumentBuilder.getCustomData().toBuilder().putFields("wiki_metadata", wikiDataValue).build();
        return pipeDocumentBuilder.setCustomData(customData).build();
    }

    String parseDateParsed(Timestamp dateParsed) {
        Instant date = Instant.ofEpochSecond(dateParsed.getSeconds(), dateParsed.getNanos());
        DateTimeFormatter formatter = ISO_INSTANT.withZone(ZoneId.from(ZoneOffset.UTC));
        return formatter.format(date);
    }

}
