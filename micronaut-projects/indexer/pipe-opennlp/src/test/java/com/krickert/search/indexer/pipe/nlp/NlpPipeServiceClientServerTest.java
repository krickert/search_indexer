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

import java.util.Map;

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
        Map<String, PipeDocument> docs = TestDataHelper.getFewHunderedPipeDocumentsMap();
        PipeDocument amadeus = docs.get("41525");
        PipeDocument luciferians = docs.get("41526");
        PipeDocument cod = docs.get("41515");
        PipeDocument phaseAngle = docs.get("41508");
        PipeDocument summerSolstace = docs.get("41516");


        PipeReply reply1 = nlpService.send(createPipeRequest(amadeus));
        assertThat(reply1).isNotNull();
        assertThat(reply1.getDocument().getSentencesList()).hasSize(94);
        PipeReply reply2 = nlpService.send(createPipeRequest(luciferians));
        assertThat(reply2).isNotNull();
        assertThat(reply2.getDocument().getSentencesList()).hasSize(1);
        PipeReply reply3 = nlpService.send(createPipeRequest(cod));
        assertThat(reply3).isNotNull();
        assertThat(reply3.getDocument().getSentencesList()).hasSize(136);
        PipeReply reply4 = nlpService.send(createPipeRequest(phaseAngle));
        assertThat(reply4).isNotNull();
        assertThat(reply4.getDocument().getSentencesList()).hasSize(1);
        PipeReply reply5 = nlpService.send(createPipeRequest(summerSolstace));
        assertThat(reply5).isNotNull();
        assertThat(reply5.getDocument().getSentencesList()).hasSize(1);
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
