package com.krickert.search;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;

public class PipeStep implements Step<PipeDocument, PipeDocument> {

    private final PipeServiceGrpc.PipeServiceBlockingStub pipeService;

    public PipeStep(PipeServiceGrpc.PipeServiceBlockingStub pipeService) {
        this.pipeService = pipeService;
    }

    @Override
    public PipeDocument execute(PipeDocument value) {
        PipeRequest request = createRequest(value);
        PipeReply reply = pipeService.send(request);
        return reply.getDocument();
    }

    private PipeRequest createRequest(PipeDocument value) {
        return null;
    }

    @Override
    public <R> Step<PipeDocument, R> pipe(Step<PipeDocument, R> source) {
        return Step.super.pipe(source);
    }
}
