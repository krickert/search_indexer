package com.krickert.search.vectorizer.grpc;

import com.krickert.search.service.EmbeddingsVectorRequest;
import com.krickert.search.service.EmbeddingsVectorsRequest;
import com.krickert.search.service.EmbeddingsVectorReply;
import com.krickert.search.service.EmbeddingsVectorsReply;
import com.krickert.search.service.EmbeddingServiceGrpc;
import com.krickert.search.vectorizer.Vectorizer;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class EmbeddingsEndpoint extends EmbeddingServiceGrpc.EmbeddingServiceImplBase {
    private final Vectorizer vectorizer;

    public EmbeddingsEndpoint(Vectorizer vectorizer) {
        this.vectorizer = vectorizer;
    }

    @Override
    public void createEmbeddingsVector(EmbeddingsVectorRequest request, StreamObserver<EmbeddingsVectorReply> responseObserver) {
        EmbeddingsVectorReply.Builder builder = EmbeddingsVectorReply.newBuilder();
        Collection<Float> embeddings = vectorizer.getEmbeddings(request.getText());
        builder.addAllEmbeddings(embeddings);
        EmbeddingsVectorReply reply = builder.build();
        sendReply(responseObserver, reply);
    }

    @Override
    public void createEmbeddingsVectors(EmbeddingsVectorsRequest request, StreamObserver<EmbeddingsVectorsReply> responseObserver) {
        EmbeddingsVectorsReply.Builder builder = EmbeddingsVectorsReply.newBuilder();
        List<EmbeddingsVectorReply> embeddings = request.getTextList().parallelStream()
                .map(text -> {
                    Collection<Float> vector = vectorizer.getEmbeddings(text);
                    EmbeddingsVectorReply.Builder replyBuilder = EmbeddingsVectorReply.newBuilder();
                    replyBuilder.addAllEmbeddings(vector);
                    return replyBuilder.build();
                })
                .collect(Collectors.toList());
        builder.addAllEmbeddings(embeddings);
        sendReply(responseObserver, builder.build());
    }

    /**
     * Sends the reply to the response observer.
     *
     * @param responseObserver The response observer to send the reply.
     * @param reply The reply to be sent.
     */
    private <T> void sendReply(StreamObserver<T> responseObserver, T reply) {
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }


}
