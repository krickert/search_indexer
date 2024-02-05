package com.krickert.search.pipeline.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.pipeline.component.PipelineProcessor;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static com.krickert.search.model.util.ProtobufUtils.createKey;

/**
 * This class is a Kafka listener that receives and processes search pipeline documents from Kafka,
 * and sends the output of the pipeline to the enhanced document topic.
 */
@KafkaListener(threads = 15,
              groupId = "pipeline-document-listener-1",
              offsetReset = OffsetReset.EARLIEST,
              properties = {
                @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                              value = KafkaProtobufConstants.PIPE_DOCUMENT_CLASS),
                      @Property(name = "max.request.size", value = "33539174")
              }, batch = true)
@Prototype
public class PipelineDocumentListener {
    private final PipelineProcessor processor;
    private final EnhancedDocumentProducer producer;
    private static final Logger log = LoggerFactory.getLogger(PipelineDocumentListener.class);

    @Inject
    public PipelineDocumentListener(PipelineProcessor processor, EnhancedDocumentProducer producer) {
        this.processor = processor;
        this.producer = producer;
    }

    /**
     * Receives and processes a search pipeline document from Kafka and sends the output of the pipeline to the
     * enhanced document topic.
     * @throws NullPointerException if the key or request is null.
     * @throws IllegalArgumentException if the offset or partition is negative.
     */
    @Topic("pipeline-document")
    public void receiveSearchPipeline(List<ConsumerRecord<UUID, PipeDocument>> records) {
        log.info("calculating {} records", records.size());
        records.parallelStream().forEach(record -> {
            try {
                UUID key = record.key();
                PipeDocument request = record.value();
                long offset = record.offset();
                int partition = record.partition();
                String topic = record.topic();
                long timestamp = record.timestamp();
                log.debug("Got the request {} with key {}", request, key);
                log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                        request.getTitle(), offset, partition, topic, timestamp);
                PipeDocument pipeDocument = processor.process(request, "search-pipeline");
                producer.sendEnhancedDocument(createKey(pipeDocument), pipeDocument);
            } catch (RuntimeException rte) {
                log.error("Error processing search pipeline document", rte);
            }
        });
    }



}
