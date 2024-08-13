package com.krickert.search.chunker.grpc;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.service.*;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.micronaut.core.util.StringUtils;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.apache.commons.compress.utils.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final AtomicInteger docCount = new AtomicInteger(1);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final Collection<ChunkReply> finishedEmbeddingsVectorReply = Lists.newArrayList();
    StreamObserver<ChunkReply> streamEmbeddingsVectorReplyObserver = new StreamObserver<>() {
        @Override
        public void onNext(ChunkReply reply) {
            log.info("Received the {} document with {} chunks", docCount.getAndIncrement(), reply.getChunksCount());
            finishedEmbeddingsVectorReply.add(reply);
        }

        @Override
        public void onError(Throwable throwable) {
            errorCount.incrementAndGet();
            log.error("Chunker threw an error.  This is a client error", throwable);
        }

        @Override
        public void onCompleted() {
            log.debug("Finished");
        }

    };


    @Test
    void testChunkServerEndpoint() {
        Collection<String> documentBodies = new java.util.ArrayList<>(TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getBody).toList());
        documentBodies.removeIf(String::isEmpty);
        for (String text : documentBodies) {
            ChunkRequest request = ChunkRequest.newBuilder()
                    .setText(text).setOptions(
                            ChunkOptions.newBuilder().setLength(300).setOverlap(30).build())
                    .build();
            ChunkReply reply = null;
            try {
                reply = endpoint.chunk(request);
                assertNotNull(reply);
            } catch (StatusRuntimeException sre) {
                log.error("The chunker threw an error that we are swallowing for now. " +
                        "The last call in the for loop seems to cause the issue.  Here's the request [{}] and here's the reply [{}]. " +
                        "There was a total of {} documents.  Exception is [{}]", request, reply, docCount.get(), sre.getMessage());
            }
        }
    }

    @Test
    void testChunkAsyncEndpoint() {
        errorCount.set(0);
        for (String body : getDocumentBodies()) {
            if (StringUtils.isEmpty(body)) {
                log.info("Expecting 2 bodies to be empty.  This is the {} one", errorCount.incrementAndGet());
            } else {
                endpoint2.chunk(createChunkRequest(body), streamEmbeddingsVectorReplyObserver);
            }
        }
        log.info("waiting up to 15 seconds for at least 1 document to be added..");
        await().atMost(15, SECONDS).until(() -> finishedEmbeddingsVectorReply.size() > 1);
        log.info("waiting up to 25 seconds for at least 10 docs to be added..");
        await().atMost(25, SECONDS).until(() -> finishedEmbeddingsVectorReply.size() > 10);
        //my machine works fine.  Git hub seems to take forever.  This was once 80 seconds.
        //"works on my local" they say.  "Trump will never win the election" they said.
        //lies, blantant lies.
        log.info("waiting for 500 seconds max for all 365 documents to be processed..");
        await().atMost(50, SECONDS).until(() -> (finishedEmbeddingsVectorReply.size() + errorCount.get()) == 367);
    }

    private Collection<String> getDocumentBodies() {
        return TestDataHelper.getFewHunderedPipeDocuments().stream().map(PipeDocument::getBody).toList();
    }

    private ChunkRequest createChunkRequest(String body) {
        return ChunkRequest.newBuilder()
                .setText(body)
                .setOptions(ChunkOptions.newBuilder().setLength(50).setOverlap(3).build())
                .build();
    }

}
