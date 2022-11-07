package wiki.dump.file.processor.component;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import wiki.dump.file.processor.messaging.WikiArticleProducer;

import java.io.IOException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Prototype
public class WikiArticleFilter implements IArticleFilter {

    final WikiArticleProducer producer;
    final WikiMarkupCleaner cleaner;
    final WikiURLExtractor urlExtractor;

    WikiArticleFilter(WikiArticleProducer producer, WikiMarkupCleaner cleaner, WikiURLExtractor urlExtractor) {
        this.producer = checkNotNull(producer);
        this.cleaner = checkNotNull(cleaner);
        this.urlExtractor = checkNotNull(urlExtractor);
    }

    @Override
    public void process(WikiArticle article, Siteinfo siteinfo) throws IOException {
        log.info("Sending {}:{}", article.getId(),article.getTitle());
        producer.sendParsedArticleProcessingRequest(UUID.randomUUID(),
                com.krickert.search.model.wiki.WikiArticle.newBuilder()
                        .setId(article.getId())
                        .setNamespace(article.getNamespace())
                        .setNamespaceCode(article.getIntegerNamespace())
                        .setRevisionId(article.getRevisionId())
                        .setSiteInfo(
                                com.krickert.search.model.wiki.WikiSiteInfo.newBuilder()
                                        .setBase(siteinfo.getBase())
                                        .setGenerator(siteinfo.getGenerator())
                                        .setSiteName(siteinfo.getSitename())
                                        .setCharacterCase(siteinfo.getCharacterCase())
                                        .build())
                        .setWikiText(article.getText())
                        .setText(
                                cleaner.extractCleanTestFromWiki(article.getText()))
                        .setDumpTimestamp(article.getTimeStamp())
                        .setTitle(article.getTitle())
                        .setRevisionId(article.getRevisionId())
                        .addAllUrlReferences(urlExtractor.parseUrlEntries(article.getText()))
                        .build());
    }
}
