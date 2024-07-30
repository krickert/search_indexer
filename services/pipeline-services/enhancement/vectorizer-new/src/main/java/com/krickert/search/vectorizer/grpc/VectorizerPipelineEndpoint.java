package com.krickert.search.vectorizer.grpc;


import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.*;
import com.krickert.search.vectorizer.Vectorizer;
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
public class VectorizerPipelineEndpoint extends PipeServiceGrpc.PipeServiceImplBase {
    private final Vectorizer vectorizer;

    /**
     * This class represents an endpoint for the Vectorizer service.
     * It is responsible for preparing documents, creating embeddings,
     * and sending the reply to the response observer.
     */
    @Inject
    public VectorizerPipelineEndpoint(Vectorizer vectorizer) {
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
        PipeDocument.Builder document = prepareDocument(req.getDocument());
        PipeReply reply = prepareReply(document);
        sendReply(responseObserver, reply);
    }

    /**
     * Prepares a document by creating embeddings and merging custom data, based on the given request.
     *
     * @param req The request for sending the document.
     * @return The prepared document.
     */
    public PipeDocument.Builder prepareDocument(PipeDocument req) {
        PipeDocument.Builder document = req.toBuilder();
        Map<String, Value> embeddingsToUpdate = createEmbeddingsMapForDocument(document);
        document.mergeCustomData(Struct.newBuilder().putAllFields(embeddingsToUpdate).build());
        return document;
    }

    /**
     * Creates embeddings for a given document.
     *
     * @param document The document for which to create embeddings.
     * @return A map that contains the embeddings update.
     */
    private Map<String, Value> createEmbeddingsMapForDocument(PipeDocument.Builder document) {
        Map<String, Value> embeddingsMap = new HashMap<>();
        embeddingsMap.put("embeddings", createEmbedding(document.getBody()));
        ListValue.Builder paragraphEmbeddings = ListValue.newBuilder();
        for (int i = 0; i < document.getBodyParagraphsCount(); i++) {
            paragraphEmbeddings.addValues(createEmbedding(document.getBodyParagraphs(i)));
        }
        embeddingsMap.put("paragraphEmbeddings", Value.newBuilder().setListValue(paragraphEmbeddings.build()).build());
        return embeddingsMap;
    }

    private Value createEmbedding(String inputText) {
        ListValue values = convertEmbeddingsToListValue(vectorizer.getEmbeddings(inputText));
        return Value.newBuilder().setListValue(values).build();
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
