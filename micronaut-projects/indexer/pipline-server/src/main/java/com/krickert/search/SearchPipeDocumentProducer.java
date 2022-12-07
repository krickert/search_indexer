package com.krickert.search;
import com.krickert.search.model.pipe.PipeDocument;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient
public interface SearchPipeDocumentProducer {

    @Topic("search-pipeline-document")
    void sendEnhancedPipeDocument(@KafkaKey UUID key, PipeDocument doc);

}