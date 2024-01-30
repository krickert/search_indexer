package com.krickert.search.pipeline;

import com.google.common.collect.Lists;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.test.util.TestDataHelper;
import com.krickert.search.pipeline.component.PipelineProcessor;
import com.krickert.search.service.PipeReply;
import io.grpc.stub.StreamObserver;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

@MicronautTest
public class ClientTest {
    private static final Logger log = LoggerFactory.getLogger(ClientTest.class);

    @Inject
    PipelineProcessor pipelineProcessor;


    private final List<PipeReply> replies = Lists.newArrayList();



    private final StreamObserver<PipeReply> replyStreamObserver = new StreamObserver<>() {
        @Override
        public void onNext(PipeReply value) {
            replies.add(value);
            log.info(value.getDocument().getTitle());
        }

        @Override
        public void onError(Throwable t) {
            log.error("error", t);
        }

        @Override
        public void onCompleted() {
            log.info("Completed");
        }
    };

    //TODO: consul testcontainers and 2 grpc service testcontianers.
    //TODO: take the 2 grpc services we did so far and make them a testcontiner
    @Test
    public void testConsulStub() {
        Assertions.assertNotNull(pipelineProcessor);
        Collection<PipeDocument> pipeDocuments = TestDataHelper.getFewHunderedPipeDocuments();
        for (PipeDocument document : pipeDocuments ) {
            PipeDocument reply = pipelineProcessor.process(document, "test-pipeline");
            log.info(reply.getTitle());
        }
    }
}
