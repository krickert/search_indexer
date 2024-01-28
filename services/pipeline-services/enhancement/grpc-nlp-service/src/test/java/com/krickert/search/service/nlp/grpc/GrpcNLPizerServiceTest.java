package com.krickert.search.service.nlp.grpc;

import com.google.common.collect.Maps;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
class GrpcNLPizerServiceTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcNLPizerServiceTest.class);

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }



    @Inject
    PipeServiceGrpc.PipeServiceBlockingStub endpoint;

    @Inject
    PipeServiceGrpc.PipeServiceStub endpoint2;

    private final Map<String,PipeDocument> finishedDocs = Maps.newConcurrentMap();

    StreamObserver<PipeReply> streamObserver = new StreamObserver<>() {
        @Override
        public void onNext(PipeReply reply) {
            log.debug("RESPONSE, returning document: {}",  reply.getDocument().getTitle());
            finishedDocs.put(reply.getDocument().getId(), reply.getDocument());
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("Not implemented", throwable);
        }

        @Override
        public void onCompleted() {
            log.info("Finished");
        }

    };



    @Test
    void testServerEndpoint() {
        Collection<PipeDocument> documents = TestDataHelper.getFewHunderedPipeDocuments();
        for (PipeDocument doc : documents) {
            PipeRequest request = PipeRequest.newBuilder()
                    .setDocument(doc).build();
            PipeReply reply = endpoint.send(request);
            Map<String, Value> fieldsMap  = reply.getDocument().getCustomData().getFieldsMap();
            assertNotNull(fieldsMap);
        }
    }

    @Test
    void testAsyncEndpoint() {

        Collection<PipeDocument> documents = TestDataHelper.getFewHunderedPipeDocuments();
        for (PipeDocument doc : documents) {
            PipeRequest request = PipeRequest.newBuilder()
                    .setDocument(doc).build();
            endpoint2.send(request, streamObserver);

        }
        await().atMost(1000, SECONDS).until(() -> finishedDocs.size() == 367);
    }

}
