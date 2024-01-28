package com.krickert.search.pipeline.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.pipeline.component.PipelineProcessor;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.krickert.search.model.util.ProtobufUtils.createKey;

/**
 * This class is a Kafka listener that receives and processes search pipeline documents from Kafka,
 * and sends the output of the pipeline to the enhanced document topic.
 */
@KafkaListener(threads = 10,
              groupId = "pipeline-document-listener",
              properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
              value = KafkaProtobufConstants.PIPE_DOCUMENT_CLASS))
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
     *
     * @param key       The UUID key of the message.
     * @param request   The search pipeline document to process.
     * @param offset    The offset of the message.
     * @param partition The partition of the message.
     * @param topic     The topic of the message.
     * @param timestamp The timestamp of the message.
     *
     * @throws NullPointerException if the key or request is null.
     * @throws IllegalArgumentException if the offset or partition is negative.
     */
    @Topic("pipeline-document")
    public void receiveSearchPipeline(@KafkaKey UUID key,
                        PipeDocument request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with key {}", request, key);
        log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                request.getTitle(), offset, partition, topic, timestamp);
        PipeDocument pipeDocument = processor.process(request,"search-pipeline");
        producer.sendEnhancedDocument(createKey(pipeDocument), pipeDocument);
    }



}
