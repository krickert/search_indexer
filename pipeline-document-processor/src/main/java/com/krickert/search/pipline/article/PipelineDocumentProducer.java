package com.krickert.search.pipline.article;

import com.krickert.search.model.PipelineDocument;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "pipeline-document-producer")
public interface PipelineDocumentProducer {
    @Topic("pipeline-document")
    void sendDocument(@KafkaKey UUID key, PipelineDocument doc);
}
