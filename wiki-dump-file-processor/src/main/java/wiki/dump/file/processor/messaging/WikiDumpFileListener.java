package wiki.dump.file.processor.messaging;
import com.krickert.search.model.wiki.DownloadedFile;
import info.bliki.wiki.dump.WikiXMLParser;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xml.sax.SAXException;
import wiki.dump.file.processor.component.WikiArticleFilter;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@KafkaListener(offsetReset = OffsetReset.EARLIEST, threads = 8)
@Prototype
public class WikiDumpFileListener {

    final WikiArticleFilter wikiArticleFilter;

    @Inject
    public WikiDumpFileListener(WikiArticleFilter wikiArticleFilter) {
        this.wikiArticleFilter = wikiArticleFilter;
    }


    @Topic("wiki-dump-file")
    public void receive(@KafkaKey UUID uuid,
                        DownloadedFile request,
                        long offset,
                        int partition,
                        String topic,
                        long timestamp) {
        log.debug("Got the request {} with UUID {}", request, uuid.toString());
        log.info("this {} was sent {} ago from partition {} from the {} topic at {}",
                request.getFullFilePath(), offset, partition, topic, timestamp);

        final WikiXMLParser parser;
        try {
            parser = new WikiXMLParser(new File(request.getFullFilePath()), wikiArticleFilter);
            parser.parse();
        } catch (IOException | SAXException e) {
            log.error("Problem with parsing file {}.  Error returned: {}", request, ExceptionUtils.getStackTrace(e));
        }
    }
}