package com.krickert.search.pipeline.messaging;

import com.krickert.search.model.pipe.PipeDocument;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;


/**
 * A Kafka client used for producing enhanced documents.
 *
 * <p>
 * The EnhancedDocumentProducer interface defines a method for sending enhanced documents to a Kafka topic.
 * This interface is annotated with {@link KafkaClient}, specifying its client ID as "enhanced-document-producer".
 * </p>
 *
 * <p>
 * Usage:
 * <ul>
 *   <li>Create an instance of a class that implements the EnhancedDocumentProducer interface.
 *   <li>Use the {@link #sendEnhancedDocument(UUID, PipeDocument)} method to send an enhanced document to the Kafka topic.
 * </ul>
 * </p>
 */
@KafkaClient(id = "enhanced-document-producer")
public interface EnhancedDocumentProducer {

    /**
     * Sends an enhanced document to the Kafka topic.
     *
     * @param key the UUID key associated with the document
     * @param request the PipeDocument object representing the enhanced document
     */
    @Topic("enhanced-document")
    void sendEnhancedDocument(@KafkaKey UUID key, PipeDocument request);

}
