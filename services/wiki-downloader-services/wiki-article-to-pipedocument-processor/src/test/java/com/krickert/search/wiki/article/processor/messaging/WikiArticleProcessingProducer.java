package com.krickert.search.wiki.article.processor.messaging;

import com.krickert.search.model.wiki.WikiArticle;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "parsed-article-producer")
public interface WikiArticleProcessingProducer {
    @Topic("wiki-parsed-article")
    void sendFileProcessingRequest(@KafkaKey UUID key, WikiArticle request);
}