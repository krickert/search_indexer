package com.krickert.search.parser.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.parser.tika.ParsedDocument;
import com.krickert.search.parser.tika.RawDocumentRequest;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.UUID;

import static com.krickert.search.model.util.ProtobufUtils.createKey;
import static com.krickert.search.parser.component.DocumentParser.parseDocument;

@KafkaListener(offsetReset = OffsetReset.EARLIEST, threads = 8,
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.RAW_DOCUMENT_REQUEST))
@Prototype
public class RawDocumentListener {
    private static final Logger log = LoggerFactory.getLogger(RawDocumentListener.class);
    private final ParsedDocumentProducer parsedDocumentProducer;

    @Inject
    public RawDocumentListener(ParsedDocumentProducer parsedDocumentProducer) {
        this.parsedDocumentProducer = parsedDocumentProducer;
    }

    @Topic("raw-document")
    public void receive(@KafkaKey UUID key,
                        RawDocumentRequest request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with key {}", request, key);
        log.info("raw bytes recieved: was sent with offset {} from partition {} from the {} topic at {}",
                 offset, partition, topic, timestamp);
        ParsedDocument doc = null;
        try {
            doc = parseDocument(request.getContent()).getDoc();
            parsedDocumentProducer.sendParsedDocument(createKey(doc), doc);
        } catch (IOException | SAXException | TikaException e) {
            throw new RuntimeException(e);
        }


    }
}
