package com.krickert.search.download.request.messaging;

import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

/**
 * The DownloadRequestProducer interface represents a Kafka producer that is responsible for sending download requests to the topic "download-request".
 * This interface is annotated with @KafkaClient to indicate that it is a Kafka client.
 */
@KafkaClient
public interface DownloadRequestProducer {
    /**
     * Sends a download request to the "download-request" topic.
     *
     * @param key The UUID key associated with the request.
     * @param request The DownloadFileRequest object containing the details of the download request.
     *
     * @topic download-request
     */
    @Topic("download-request")
    void sendDownloadRequest(@KafkaKey UUID key, DownloadFileRequest request);

}