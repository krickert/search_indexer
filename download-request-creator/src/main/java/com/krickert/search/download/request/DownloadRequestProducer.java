package com.krickert.search.download.request;
import com.krickert.search.model.wiki.DownloadFileRequest;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient
public interface DownloadRequestProducer {
    @Topic("download-request")
    void sendDownloadRequest(@KafkaKey UUID key, DownloadFileRequest request);

}