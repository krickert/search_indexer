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


@Singleton
public class VectorizerEndpoint extends PipeServiceGrpc.PipeServiceImplBase {

    @Inject
    Vectorizer vectorizer;

    @Override
    public void send(PipeRequest req, StreamObserver<PipeReply> responseObserver) {
        PipeDocument.Builder document = req.getDocument().toBuilder();
        Struct.Builder customDataBuilder = document.getCustomDataBuilder();
        ListValue values = createListOfFloats(vectorizer.getEmbeddings(document.getBody()));
        customDataBuilder.putFields("embeddings",  Value.newBuilder().setListValue(values).build());
        document.mergeCustomData(customDataBuilder.build());
        PipeReply reply =  PipeReply.newBuilder()
                .setDocument(document).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private ListValue createListOfFloats(Collection<Float> embeddings) {
        final ListValue.Builder returnVal = ListValue.newBuilder();
        for (Float embedding : embeddings) {
            returnVal.addValues(Value.newBuilder().setNumberValue(embedding.doubleValue()));
        }
        return returnVal.build();
    }

}
