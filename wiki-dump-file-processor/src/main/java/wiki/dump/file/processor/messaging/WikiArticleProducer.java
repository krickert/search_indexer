package wiki.dump.file.processor.messaging;
import com.krickert.search.model.ParsedWikiArticle;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;

import java.util.UUID;

@KafkaClient(id = "wiki-article-producer")
public interface WikiArticleProducer {

    @Topic("wiki-parsed-article")
    void sendParsedArticleProcessingRequest(@KafkaKey UUID key, ParsedWikiArticle request);

}