package com.krickert.search.service.vectorizer.grpc;


import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import com.krickert.search.service.vectorizer.Vectorizer;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents an endpoint for a vectorizer service.
 */
@Singleton
public class VectorizerEndpoint extends PipeServiceGrpc.PipeServiceImplBase {
    private final Vectorizer vectorizer;

    /**
     * This class represents an endpoint for the Vectorizer service.
     * It is responsible for preparing documents, creating embeddings,
     * and sending the reply to the response observer.
     */
    @Inject
    public VectorizerEndpoint(Vectorizer vectorizer) {
        this.vectorizer = vectorizer;
    }

    /**
     * Prepares a document, creates embeddings, prepares a reply, and sends the reply to the response observer.
     *
     * @param req The request for sending the document.
     * @param responseObserver The response observer to send the reply.
     */
    @Override
    public void send(PipeRequest req, StreamObserver<PipeReply> responseObserver) {
        PipeDocument.Builder document = prepareDocument(req);
        PipeReply reply = prepareReply(document);
        sendReply(responseObserver, reply);
    }

    /**
     * Prepares a document by creating embeddings and merging custom data, based on the given request.
     *
     * @param req The request for sending the document.
     * @return The prepared document.
     */
    private PipeDocument.Builder prepareDocument(PipeRequest req) {
        PipeDocument.Builder document = req.getDocument().toBuilder();
        Map<String, Value> embeddingsToUpdate = createEmbeddings(document);
        document.mergeCustomData(Struct.newBuilder().putAllFields(embeddingsToUpdate).build());
        return document;
    }

    /**
     * Creates embeddings for a given document.
     *
     * @param document The document for which to create embeddings.
     * @return A map that contains the embeddings update.
     */
    private Map<String, Value> createEmbeddings(PipeDocument.Builder document) {
        Map<String, Value> embeddingsToUpdate = new HashMap<>();
        ListValue values = convertEmbeddingsToListValue(vectorizer.getEmbeddings(document.getBody()));
        embeddingsToUpdate.put("embeddings", Value.newBuilder().setListValue(values).build());
        return embeddingsToUpdate;
    }

    /**
     * Prepares a reply using the given document builder.
     *
     * @param document The document builder to create the reply.
     * @return The prepared reply.
     */
    private PipeReply prepareReply(PipeDocument.Builder document) {
        return PipeReply.newBuilder().setDocument(document).build();
    }

    /**
     * Sends the reply to the response observer.
     *
     * @param responseObserver The response observer to send the reply.
     * @param reply The reply to be sent.
     */
    private void sendReply(StreamObserver<PipeReply> responseObserver, PipeReply reply) {
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    /**
     * Converts a collection of embeddings to a ListValue.
     *
     * @param embeddings The collection of embeddings to convert.
     * @return The converted ListValue.
     */
    private ListValue convertEmbeddingsToListValue(Collection<Float> embeddings) {
        final ListValue.Builder returnVal = ListValue.newBuilder();
        for (Float embedding : embeddings) {
            returnVal.addValues(Value.newBuilder().setNumberValue(embedding.doubleValue()));
        }
        return returnVal.build();
    }
}