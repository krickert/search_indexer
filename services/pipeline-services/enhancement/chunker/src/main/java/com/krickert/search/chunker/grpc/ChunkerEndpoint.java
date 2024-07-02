package com.krickert.search.chunker.grpc;


import com.krickert.search.chunker.OverlapChunker;
import com.krickert.search.service.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collection;


/**
 * ChunkerEndpoint class is a gRPC service implementation that handles chunking of text.
 */
@Singleton
public class ChunkerEndpoint extends ChunkServiceGrpc.ChunkServiceImplBase {
    private final OverlapChunker chunker;

    @Inject
    public ChunkerEndpoint(OverlapChunker chunker) {
        this.chunker = chunker;
    }

    @Override
    public void chunk(ChunkRequest request, StreamObserver<ChunkReply> responseObserver) {
        Status error = validateRequest(request);
        if (error != null) {
            responseObserver.onError(error.asRuntimeException());
            return;
        }

        Collection<String> chunkedTexts = chunker.chunkText(request.getText(), request.getOptions().getLength(), request.getOptions().getOverlap());

        ChunkReply reply = ChunkReply.newBuilder()
                .addAllChunks(chunkedTexts)
                .build();

        sendReply(responseObserver, reply);
    }

    private Status validateRequest(ChunkRequest request) {
        if (!request.hasOptions()) {
            return Status.INVALID_ARGUMENT.withDescription("ChunkOptions is required");
        }

        ChunkOptions options = request.getOptions();
        if (options.getLength() <= 0) {
            return Status.INVALID_ARGUMENT.withDescription("Invalid length value " + options.getLength());
        }

        if (options.getOverlap() < 0 || options.getOverlap() > options.getLength()) {
            return Status.INVALID_ARGUMENT
                    .withDescription("Invalid overlap value. Overlap: {" + options.getOverlap() + "}. Length: {" + options.getLength() + "}");
        }

        if (request.getText().isEmpty()) {
            return Status.INVALID_ARGUMENT.withDescription("Text is empty");
        }

        return null;
    }

    private <T> void sendReply(StreamObserver<T> responseObserver, T reply) {
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
