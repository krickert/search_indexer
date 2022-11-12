package com.krickert.search.wiki.dump.file.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.wiki.WikiSiteInfo;
import com.krickert.search.model.wiki.WikiType;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.krickert.search.wiki.dump.file.messaging.WikiArticleProducer;

import java.io.IOException;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static com.krickert.search.model.util.ProtobufUtils.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

@Prototype
public class WikiArticleFilter implements IArticleFilter {
    private static final Logger log = LoggerFactory.getLogger(WikiArticleFilter.class);

    final WikiArticleProducer producer;
    final WikiMarkupCleaner cleaner;
    final WikiURLExtractor urlExtractor;


    public WikiArticleFilter(WikiArticleProducer producer, WikiMarkupCleaner cleaner, WikiURLExtractor urlExtractor) {
        this.producer = checkNotNull(producer);
        this.cleaner = checkNotNull(cleaner);
        this.urlExtractor = checkNotNull(urlExtractor);
    }

    @Override
    public void process(WikiArticle article, Siteinfo siteinfo) throws IOException {
        log.info("Sending {}:{}", article.getId(),article.getTitle());
        com.krickert.search.model.wiki.WikiArticle protoArticle = createWikiArticleProto(article, siteinfo);
        producer.sendParsedArticleProcessingRequest(createKey(protoArticle.getId()), protoArticle);
    }


    @NotNull
    private com.krickert.search.model.wiki.WikiArticle createWikiArticleProto(WikiArticle article, Siteinfo siteinfo) {
        com.krickert.search.model.wiki.WikiArticle.Builder builder =  com.krickert.search.model.wiki.WikiArticle.newBuilder()
                .setId(article.getId())
                .setNamespace(article.getNamespace())
                .setNamespaceCode(article.getIntegerNamespace())
                .setRevisionId(article.getRevisionId())
                .setSiteInfo(
                        WikiSiteInfo.newBuilder()
                                .setBase(siteinfo.getBase())
                                .setGenerator(siteinfo.getGenerator())
                                .setSiteName(siteinfo.getSitename())
                                .setCharacterCase(siteinfo.getCharacterCase())
                                .build());
        if (notNull(article.getText())) {
            builder.setWikiText(article.getText())
                    .setText(cleaner.extractCleanTestFromWiki(article.getText()))
                    .addAllUrlReferences(urlExtractor.parseUrlEntries(article.getText()));
        }
        if (notNull(article.getTimeStamp())) {
            try {
                String timestampStr = article.getTimeStamp();
                Instant in = Instant.from(ISO_INSTANT.parse(timestampStr));
                Timestamp created = com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(in.getEpochSecond())
                        .setNanos(in.getNano()).build();
                builder.setTimestamp(created);
            } catch (RuntimeException e ) {
                log.error("illegal format for dates", e);
            }
        }
        return builder.setDumpTimestamp(article.getTimeStamp())
        .setTitle(article.getTitle())
        .setWikiType(findWikiCategory(article.getTitle(), article.getText()))
        .setDateParsed(now())
        .build();
    }

    private WikiType findWikiCategory(String title, String wikiBody) {
        if (title.contains("REDIRECT")
                || (StringUtils.isNotEmpty(wikiBody) &&
                    ( wikiBody.startsWith("#REDIRECT ")
                    || wikiBody.startsWith("#redirect "))))
        {
            return WikiType.REDIRECT;
        } else if (title.startsWith("Category:")) {
            return WikiType.CATEGORY;
        } else if (title.startsWith("List of")) {
            return WikiType.LIST;
        } else if (title.startsWith("Wikipedia:")) {
            return WikiType.WIKIPEDIA;
        } else if (title.startsWith("Draft:")) {
            return WikiType.DRAFT;
        } else if (title.startsWith("Template:")) {
            return WikiType.TEMPLATE;
        } else if (title.startsWith("File:")) {
            return WikiType.FILE;
        } else {
            return WikiType.ARTICLE;
        }
    }

    private static boolean notNull(Object o) {
        return o != null;
    }
}
