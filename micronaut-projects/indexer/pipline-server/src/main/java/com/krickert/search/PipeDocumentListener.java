package com.krickert.search;
import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.WikiArticle;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;

import java.util.UUID;

@KafkaListener(offsetReset = OffsetReset.EARLIEST,
        threads = 8,
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.PIPE_DOCUMENT_CLASS))
public class PipeDocumentListener {

    @Topic("pipe-document")
    public void receive(@KafkaKey UUID key,
                        WikiArticle request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {

    }

}