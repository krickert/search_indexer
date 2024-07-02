package com.krickert.search.chunker.grpc;

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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class GrpcChunkerServiceTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcChunkerServiceTest.class);

    @Inject
    EmbeddedApplication<?> application;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }



    @Inject
    ChunkServiceGrpc.ChunkServiceBlockingStub endpoint;
    @Inject
    ChunkServiceGrpc.ChunkServiceStub endpoint2;

    private final Collection<ChunkReply> finishedEmbeddingsVectorReply = Lists.newArrayList();
    StreamObserver<ChunkReply> streamEmbeddingsVectorReplyObserver = new StreamObserver<>() {
        @Override
        public void onNext(ChunkReply reply) {
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

    };


    @Test
    void testChunkServerEndpoint() {
        Collection<String> documentBodies = new java.util.ArrayList<>(TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getBody).toList());
        documentBodies.removeIf(String::isEmpty);
        for (String text : documentBodies) {
            ChunkRequest request = ChunkRequest.newBuilder()
                    .setText(text).setOptions(
                            ChunkOptions.newBuilder().setLength(30).setOverlap(3).build())
                    .build();
            ChunkReply reply = endpoint.chunk(request);
            assertNotNull(reply);
        }
    }

    @Test
    void testChunkAsyncEndpoint() {

        Collection<String> titles = TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getTitle).toList();
        for (String title : titles) {
            ChunkRequest request = ChunkRequest.newBuilder()
                    .setText(title).setOptions(
                            ChunkOptions.newBuilder().setLength(50).setOverlap(3).build())
                    .build();
            endpoint2.chunk(request, streamEmbeddingsVectorReplyObserver);

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
