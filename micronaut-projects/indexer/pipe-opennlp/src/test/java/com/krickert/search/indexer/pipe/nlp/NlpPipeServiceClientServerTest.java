package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.model.util.ProtobufUtils;
import com.krickert.search.service.FieldMapping;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.channels.Pipe;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@MicronautTest
public class NlpPipeServiceClientServerTest {
    @Inject
    PipeServiceGrpc.PipeServiceBlockingStub nlpService;

    @Test
    void testNlpServiceRequestResponseNotNullAndReplyInstanceNoExceptions() {
        TestDataHelper.getFewHunderedPipeDocuments().forEach((article) ->
                assertThat(
                        nlpService.send(createPipeRequest(article)))
                        .isNotNull()
                        .isInstanceOf(PipeReply.class));

    }

    @Test
    void testNlpServiceSentenceExtraction() {
        Collection<PipeDocument> fiveDocs = TestDataHelper.getFewHunderedPipeDocuments().stream().limit(5).toList();

        Iterator<PipeDocument> docs = fiveDocs.iterator();

        PipeDocument doc1 = docs.next();
        PipeDocument doc2 = docs.next();
        PipeDocument doc3 = docs.next();
        PipeDocument doc4 = docs.next();
        PipeDocument doc5 = docs.next();


        PipeReply reply1 = nlpService.send(createPipeRequest(doc1));
        assertThat(reply1).isNotNull();
        assertThat(reply1.getDocument().getSentencesList()).hasSize(1);
        PipeReply reply2 = nlpService.send(createPipeRequest(doc2));
        assertThat(reply2).isNotNull();
        assertThat(reply2.getDocument().getSentencesList()).hasSize(5);
        PipeReply reply3 = nlpService.send(createPipeRequest(doc3));
        assertThat(reply3).isNotNull();
        assertThat(reply3.getDocument().getSentencesList()).hasSize(3);
        PipeReply reply4 = nlpService.send(createPipeRequest(doc4));
        assertThat(reply4).isNotNull();
        assertThat(reply4.getDocument().getSentencesList()).hasSize(2);
        PipeReply reply5 = nlpService.send(createPipeRequest(doc5));
        assertThat(reply5).isNotNull();
        assertThat(reply5.getDocument().getSentencesList()).hasSize(2);
    }

    private PipeRequest createPipeRequest(PipeDocument article) {
        return PipeRequest.newBuilder()
                .setDocument(article)
                .setId(article.getId())
                .addFieldMappings(
                        FieldMapping.newBuilder()
                                .setInputField("body")
                                .setOutputField("sentences")
                                .build())
                .build();
    }
}
