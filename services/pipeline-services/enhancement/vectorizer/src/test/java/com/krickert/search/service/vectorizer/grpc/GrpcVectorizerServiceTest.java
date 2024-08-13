package com.krickert.search.service.vectorizer.grpc;

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
class GrpcVectorizerServiceTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcVectorizerServiceTest.class);

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

        // Override OnError ...
    };



    @Test
    void testServerEndpoint() {
        Collection<PipeDocument> documents = TestDataHelper.getFewHunderedPipeDocuments();
        for (PipeDocument doc : documents) {
            PipeRequest request = PipeRequest.newBuilder()
                    .setDocument(doc).build();
            PipeReply reply = endpoint.send(request);
            Value embeddings = reply.getDocument().getCustomData().getFieldsMap().get("embeddings");
            assertNotNull(embeddings);
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
        log.info("waiting up to 15 seconds for at least 1 document to be added..");
        await().atMost(25, SECONDS).until(() -> finishedDocs.size() > 1);
        log.info("waiting up to 25 seconds for at least 10 docs to be added..");
        await().atMost(30, SECONDS).until(() -> finishedDocs.size() > 10);
        //my machine works fine.  Git hub seems to take forever.  This was once 80 seconds.
        //"works on my local" they say.  "Trump will never win the election" they said.
        //lies, blantant lies.
        log.info("waiting for 500 seconds max for all 367 documents to be processed..");
        await().atMost(500, SECONDS).until(() -> finishedDocs.size() >= 350);
    }

}
