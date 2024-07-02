package com.krickert.search.service.vectorizer.grpc;

import com.google.common.collect.Maps;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.service.*;
import io.grpc.stub.StreamObserver;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class GrpcEmbeddingsServiceTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcEmbeddingsServiceTest.class);

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }



    @Inject
    EmbeddingServiceGrpc.EmbeddingServiceBlockingStub endpoint;
    @Inject
    EmbeddingServiceGrpc.EmbeddingServiceStub endpoint2;

    private final Collection<EmbeddingsVectorReply> finishedEmbeddingsVectorReply = Lists.newArrayList();
    StreamObserver<EmbeddingsVectorReply> streamEmbeddingsVectorReplyObserver = new StreamObserver<>() {
        @Override
        public void onNext(EmbeddingsVectorReply reply) {
            finishedEmbeddingsVectorReply.add(reply);
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

    private final Collection<EmbeddingsVectorsReply> finishedEmbeddingsVectorsReply = Lists.newArrayList();
    StreamObserver<EmbeddingsVectorsReply> streamEmbeddingsVectorsReplyObserver = new StreamObserver<>() {
        @Override
        public void onNext(EmbeddingsVectorsReply reply) {
            finishedEmbeddingsVectorsReply.add(reply);
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
    void testEmbeddingsVectorServerEndpoint() {
        Collection<String> documentBodies = TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getBody).toList();
        for (String text : documentBodies) {
            EmbeddingsVectorRequest request = EmbeddingsVectorRequest.newBuilder()
                    .setText(text).build();
            EmbeddingsVectorReply reply = endpoint.createEmbeddingsVector(request);
            assertNotNull(reply);
            assertTrue(reply.getEmbeddingsList().size() > 100);
        }
    }

    @Test
    void testEmbeddingsVectorAsyncEndpoint() {

        Collection<String> titles = TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getTitle).toList();
        for (String title : titles) {
            EmbeddingsVectorRequest request = EmbeddingsVectorRequest.newBuilder()
                    .setText(title).build();
            endpoint2.createEmbeddingsVector(request, streamEmbeddingsVectorReplyObserver);

        }
        log.info("waiting up to 15 seconds for at least 1 document to be added..");
        await().atMost(15, SECONDS).until(() -> finishedEmbeddingsVectorReply.size() > 1);
        log.info("waiting up to 25 seconds for at least 10 docs to be added..");
        await().atMost(25, SECONDS).until(() -> finishedEmbeddingsVectorReply.size() > 10);
        //my machine works fine.  Git hub seems to take forever.  This was once 80 seconds.
        //"works on my local" they say.  "Trump will never win the election" they said.
        //lies, blantant lies.
        log.info("waiting for 500 seconds max for all 367 documents to be processed..");
        await().atMost(500, SECONDS).until(() -> finishedEmbeddingsVectorReply.size() == 367);
    }

}
