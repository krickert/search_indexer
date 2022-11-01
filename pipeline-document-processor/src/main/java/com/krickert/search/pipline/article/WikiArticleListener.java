package com.krickert.search.pipline.article;

import com.krickert.search.model.ParsedWikiArticle;
import com.krickert.search.pipline.messaging.ParsedArticleToPipelineDocumentMapper;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.UUID;

@Slf4j
@KafkaListener(threads = 10,
        groupId = "wiki-article-to-pipeline")
@Singleton
public class WikiArticleListener {

    final ParsedArticleToPipelineDocumentMapper documentMapper;
    final PipelineDocumentProducer producer;

    @Inject
    public WikiArticleListener(ParsedArticleToPipelineDocumentMapper documentMapper, PipelineDocumentProducer producer) {
        this.documentMapper = documentMapper;
        this.producer = producer;
    }

    @Topic("wiki-parsed-article")
    public void receive(@KafkaKey UUID uuid,
                        ParsedWikiArticle request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {

        log.info("Received UUID: {} ID: {} Title: {} from partition-topic: {}-{} on {} with offset {}",
                uuid.toString(), request.getId(), request.getTitle(),
                partition, topic,
                FastDateFormat.getInstance().format(timestamp),
                offset);
        producer.sendDocument(uuid, documentMapper.mapDocument(request));
    }

}
