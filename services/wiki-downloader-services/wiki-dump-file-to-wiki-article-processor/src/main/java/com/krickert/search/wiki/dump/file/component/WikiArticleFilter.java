package com.krickert.search.wiki.dump.file.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.wiki.WikiSiteInfo;
import com.krickert.search.model.wiki.WikiType;
import com.krickert.search.wiki.dump.file.messaging.WikiArticleProducer;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static com.krickert.search.model.util.ProtobufUtils.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

/**
 * This class represents a filter for Wiki articles. It implements the IArticleFilter interface.
 * It is responsible for processing Wiki articles and sending them for further processing.
 */
@Prototype
public class WikiArticleFilter implements IArticleFilter {
    private static final Logger log = LoggerFactory.getLogger(WikiArticleFilter.class);

    final WikiArticleProducer producer;
    final WikiMarkupCleaner cleaner;
    final WikiURLExtractor urlExtractor;
    final Boolean articlesOnly;
    final WikiDumpFileCounter counter;


    @Inject
    public WikiArticleFilter(WikiArticleProducer producer, WikiMarkupCleaner cleaner, WikiURLExtractor urlExtractor, @Value("${wikipedia.send-only-articles}") Boolean articlesOnly, WikiDumpFileCounter counter) {
        this.producer = checkNotNull(producer);
        this.cleaner = checkNotNull(cleaner);
        this.urlExtractor = checkNotNull(urlExtractor);
        this.articlesOnly = checkNotNull(articlesOnly);
        this.counter = checkNotNull(counter);
    }

    private static boolean notNull(Object o) {
        return o != null;
    }

    /**
     * Process the given WikiArticle.
     * If the articlesOnly flag is set and the article is not an actual article (e.g., it is a redirect or a category),
     * the method will skip processing the article.
     * The method logs the ID and title of the processed article.
     * The method creates a proto representation of the WikiArticle and sends it for further processing using the producer.
     *
     * @param article   The WikiArticle to be processed
     * @param siteinfo  The Siteinfo of the wiki
     */
    @Override
    public void process(WikiArticle article, Siteinfo siteinfo) {
        if (articlesOnly && findWikiCategory(article.getTitle(), article.getText()) != WikiType.ARTICLE) {
            log.info("Only sending articles.  Skipping {}:{}", article.getId(), article.getTitle());
            counter.incrementDocumentSkip();
            return;
        }
        log.info("Sending {}:{}", article.getId(), article.getTitle());
        com.krickert.search.model.wiki.WikiArticle protoArticle = createWikiArticleProto(article, siteinfo);
        producer.sendParsedArticleProcessingRequest(createKey(protoArticle.getId()), protoArticle);
        counter.incrementDocumentCount();
    }

    /**
     * Creates a proto representation of a WikiArticle based on the given article and siteinfo.
     *
     * @param article   The WikiArticle to be processed
     * @param siteinfo  The Siteinfo of the wiki
     * @return A com.krickert.search.model.wiki.WikiArticle object representing the given WikiArticle
     */
    @NotNull
    private com.krickert.search.model.wiki.WikiArticle createWikiArticleProto(WikiArticle article, Siteinfo siteinfo) {
        com.krickert.search.model.wiki.WikiArticle.Builder builder = com.krickert.search.model.wiki.WikiArticle.newBuilder()
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
                Timestamp created = Timestamp.newBuilder()
                        .setSeconds(in.getEpochSecond())
                        .setNanos(in.getNano()).build();
                builder.setTimestamp(created);
            } catch (RuntimeException e) {
                log.error("illegal format for dates", e);
            }
        }
        return builder.setDumpTimestamp(article.getTimeStamp())
                .setTitle(article.getTitle())
                .setWikiType(findWikiCategory(article.getTitle(), article.getText()))
                .setDateParsed(now())
                .build();
    }

    /**
     * Finds the category of a Wikipedia article based on its title and body.
     *
     * @param title    The title of the article.
     * @param wikiBody The body of the article.
     * @return The category of the article.
     */
    private WikiType findWikiCategory(String title, String wikiBody) {
        if (title.contains("REDIRECT")
                || (StringUtils.isNotEmpty(wikiBody) &&
                (wikiBody.startsWith("#REDIRECT")
                        || wikiBody.startsWith("#redirect")))) {
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
}
