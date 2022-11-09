package wiki.dump.file.processor.component;

import com.krickert.search.model.wiki.WikiSiteInfo;
import com.krickert.search.model.wiki.WikiType;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.scheduling.annotation.Async;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.dump.file.processor.messaging.WikiArticleProducer;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.krickert.search.model.util.ProtobufUtils.now;

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
        producer.sendParsedArticleProcessingRequest(createWikiArticleProto(article, siteinfo));
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
        return builder.setDumpTimestamp(article.getTimeStamp())
        .setTitle(article.getTitle())
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

    private static boolean notNull(Object o) {
        return o != null;
    }
}
