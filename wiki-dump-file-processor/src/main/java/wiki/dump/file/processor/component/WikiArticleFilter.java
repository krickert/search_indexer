package wiki.dump.file.processor.component;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.model.wiki.WikiSiteInfo;
import com.krickert.search.model.wiki.WikiType;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import wiki.dump.file.processor.messaging.WikiArticleProducer;

import java.io.IOException;
import java.sql.Time;
import java.time.Instant;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.krickert.search.model.util.ProtobufUtils.now;

@Slf4j
@Prototype
public class WikiArticleFilter implements IArticleFilter {

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
        producer.sendParsedArticleProcessingRequest(UUID.randomUUID(),
                createWikiArticleProto(article, siteinfo));
    }

    @NotNull
    private com.krickert.search.model.wiki.WikiArticle createWikiArticleProto(WikiArticle article, Siteinfo siteinfo) {
        return com.krickert.search.model.wiki.WikiArticle.newBuilder()
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
                                .build())
                .setWikiText(article.getText())
                .setText(cleaner.extractCleanTestFromWiki(article.getText()))
                .setDumpTimestamp(article.getTimeStamp())
                .setTitle(article.getTitle())
                .setRevisionId(article.getRevisionId())
                .addAllUrlReferences(urlExtractor.parseUrlEntries(article.getText()))
                .setWikiType(findWikiCategory(article.getTitle()))
                .setDateParsed(now())
                .build();
    }

    private WikiType findWikiCategory(String title) {
        if (title.contains("REDIRECT")) {
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
