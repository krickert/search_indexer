package com.krickert.search.wiki.dump.file.messaging;

import com.krickert.search.model.constants.KafkaProtobufConstants;
import com.krickert.search.model.wiki.DownloadedFile;
import com.krickert.search.wiki.dump.file.component.WikiArticleFilter;
import com.krickert.search.wiki.dump.file.component.WikiDumpFileProcessor;
import info.bliki.wiki.dump.WikiXMLParser;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.messaging.Acknowledgement;
import jakarta.inject.Inject;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

@KafkaListener(offsetReset = OffsetReset.EARLIEST,
        properties = @Property(name = KafkaProtobufConstants.SPECIFIC_CLASS_PROPERTY,
                value = KafkaProtobufConstants.DOWNLOADED_FILE_CLASS),
        groupId = "dump-file-processor")
@Prototype
public class WikiDumpFileListener {
    private static final Logger log = LoggerFactory.getLogger(WikiDumpFileListener.class);

    final WikiDumpFileProcessor wikiDumpFileProcessor;

    @Inject
    public WikiDumpFileListener(WikiDumpFileProcessor wikiDumpFileProcessor) {
        this.wikiDumpFileProcessor = wikiDumpFileProcessor;
    }


    @Topic("wiki-dump-file")
    public void receive(DownloadedFile request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp,
                        Acknowledgement ack) {
        log.debug("Got the request {} ", request);
        log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                request.getFullFilePath(), offset, partition, topic, timestamp);
        ack.ack();
        wikiDumpFileProcessor.processJob(request);

        //TODO: re-send the request if the processing fails more than 2x?  Might need to change the model to handle this situation
    }


}