package com.krickert.search.wiki.article.processor.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.WikiArticle;
import com.krickert.search.model.wiki.WikiType;
import com.krickert.search.wiki.article.processor.component.PipelineDocumentMapper;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.krickert.search.model.util.ProtobufUtils.createKey;


@KafkaListener(offsetReset = OffsetReset.EARLIEST, threads = 8,
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.WIKIARTICLE_CLASS))
@Prototype
public class WikiParsedArticleListener {
    private static final Logger log = LoggerFactory.getLogger(WikiParsedArticleListener.class);
    private final PipeDocumentProducer pipeDocumentProducer;
    private final PipelineDocumentMapper pipeDocumentMapper;

    private final Boolean parseOnlyArticles;

    @Inject
    public WikiParsedArticleListener(PipeDocumentProducer pipeDocumentProducer, @Value("${wikipedia.parse-only-articles}") Boolean parseOnlyArticles) {
        this.pipeDocumentProducer = pipeDocumentProducer;
        this.pipeDocumentMapper = new PipelineDocumentMapper();
        this.parseOnlyArticles = parseOnlyArticles;
    }


    @Topic("wiki-parsed-article")
    public void receive(@KafkaKey UUID key,
                        WikiArticle request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with key {}", request, key);
        if (parseOnlyArticles && request.getWikiType() != WikiType.ARTICLE) {
            log.info("Not sending {} with title {} because articles are only being processed.", request.getWikiType(), request.getTitle());
            return;
        }
        log.info("this {}:{} was sent with offset {} from partition {} from the {} topic at {}",
                request.getId(), request.getTitle(), offset, partition, topic, timestamp);

        pipeDocumentProducer.sendPipeDocument(
                createKey(request),
                pipeDocumentMapper.mapWikiArticleToPipeDocument(request));
    }
}