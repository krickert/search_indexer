package wiki.dump.file.processor.component;

import com.krickert.search.model.ParsedSiteInfo;
import com.krickert.search.model.ParsedWikiArticle;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import wiki.dump.file.processor.messaging.WikiArticleProducer;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Prototype
public class WikiArticleFilter implements IArticleFilter {

    @Inject
    WikiArticleProducer producer;

    @Inject
    WikiMarkupCleaner cleaner;

    @Override
    public void process(WikiArticle article, Siteinfo siteinfo) throws IOException {
        log.info("Sending {}" + article.getId());
        producer.sendParsedArticleProcessingRequest(UUID.randomUUID(),
                ParsedWikiArticle.newBuilder()
                        .setId(article.getId())
                        .setNamespace(article.getNamespace())
                        .setNamespaceCode(article.getIntegerNamespace())
                        .setRevisionId(article.getRevisionId())
                        .setSiteInfo(
                                ParsedSiteInfo.newBuilder()
                                        .setBase(siteinfo.getBase())
                                        .setGenerator(siteinfo.getGenerator())
                                        .setSitename(siteinfo.getSitename())
                                        .setCharacterCase(siteinfo.getCharacterCase())
                                        .build())
                        .setWikiText(article.getText())
                        .setText(
                                cleaner.extractCleanTestFromWiki(article.getText()))
                        .setTimestamp(article.getTimeStamp())
                        .setTitle(article.getTitle())
                        .setRevisionId(article.getRevisionId())
                        .build());
    }
}
