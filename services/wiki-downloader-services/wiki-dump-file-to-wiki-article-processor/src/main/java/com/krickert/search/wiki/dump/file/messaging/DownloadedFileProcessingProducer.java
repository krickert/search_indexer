package com.krickert.search.wiki.dump.file.messaging;

import com.krickert.search.model.wiki.DownloadedFile;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.UUID;

@KafkaClient(id = "downloaded-file-producer",
        properties = {
                @Property(name = ProducerConfig.COMPRESSION_TYPE_CONFIG, value = "lz4"),
                @Property(name = ProducerConfig.LINGER_MS_CONFIG, value = "5"),
                @Property(name = ProducerConfig.MAX_REQUEST_SIZE_CONFIG, value = "2073741824"),
                @Property(name = ProducerConfig.BATCH_SIZE_CONFIG, value = "200000"),
                @Property(name = ProducerConfig.BUFFER_MEMORY_CONFIG, value = "322122547"),
                @Property(name = ProducerConfig.ACKS_CONFIG, value = "all")
        })
public interface DownloadedFileProcessingProducer {
    @Topic("wiki-dump-file-errors")
    void sendFileProcessingRequestError(@KafkaKey UUID key, DownloadedFile request);
}