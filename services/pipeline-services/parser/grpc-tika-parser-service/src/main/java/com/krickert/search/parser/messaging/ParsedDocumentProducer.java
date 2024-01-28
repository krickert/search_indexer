package com.krickert.search.parser.messaging;

import com.krickert.search.parser.tika.ParsedDocument;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.UUID;

@KafkaClient(id = "search-document-producer",
        properties = {
                @Property(name = ProducerConfig.COMPRESSION_TYPE_CONFIG, value = "lz4"),
                @Property(name = ProducerConfig.LINGER_MS_CONFIG, value = "5"),
                @Property(name = ProducerConfig.MAX_REQUEST_SIZE_CONFIG, value = "2073741824"),
                @Property(name = ProducerConfig.BATCH_SIZE_CONFIG, value = "200000"),
                @Property(name = ProducerConfig.BUFFER_MEMORY_CONFIG, value = "322122547"),
                @Property(name = ProducerConfig.ACKS_CONFIG, value = "0")
        })
public interface ParsedDocumentProducer {

    @Topic("parsed-document")
    void sendParsedDocument(@KafkaKey UUID key, ParsedDocument request);
}
