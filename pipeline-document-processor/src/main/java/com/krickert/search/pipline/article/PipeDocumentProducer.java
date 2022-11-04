package com.krickert.search.pipline.article;

import com.krickert.search.model.pipe.PipeDocument;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "pipe-document-producer")
public interface PipeDocumentProducer {
    @Topic("pipe-document")
    void sendDocument(@KafkaKey UUID key, PipeDocument doc);
}
