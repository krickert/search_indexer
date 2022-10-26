package com.krickert.search.download.request;
import com.krickert.search.model.DownloadFileRequest;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "download-request-producer")
public interface DownloadRequestProducer {
    @Topic("download-request")
    void sendDownloadRequest(@KafkaKey UUID key, DownloadFileRequest request);

}