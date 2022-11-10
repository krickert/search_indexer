package com.krickert.search.wiki.article.processor.messaging;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.WikiArticle;
import com.krickert.search.wiki.article.processor.component.PipelineDocumentMapper;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@KafkaListener(offsetReset = OffsetReset.EARLIEST, threads = 8,
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.DOWNLOADED_FILE_CLASS))
@Prototype
public class WikiParsedArticleListener {
    private static final Logger log = LoggerFactory.getLogger(WikiParsedArticleListener.class);
    private final PipeDocumentProducer pipeDocumentProducer;
    private final PipelineDocumentMapper pipeDocumentMapper;

    @Inject
    public WikiParsedArticleListener(PipeDocumentProducer pipeDocumentProducer) {
        this.pipeDocumentProducer = pipeDocumentProducer;
        this.pipeDocumentMapper = new PipelineDocumentMapper();
    }


    @Topic("wiki-parsed-article")
    public void receive(@KafkaKey UUID uuid,
                        WikiArticle request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with UUID {}", request, uuid.toString());
        log.info("this {}:{} was sent with offset {} from partition {} from the {} topic at {}",
                request.getId(), request.getTitle(), offset, partition, topic, timestamp);

        pipeDocumentProducer.sendSearchDocument(pipeDocumentMapper.mapWikiArticleToPipeDocument(request));
    }
}