package com.krickert.search.service.grpc;


import com.google.common.collect.Maps;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import com.krickert.search.service.Vectorizer;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Map;


@Singleton
public class VectorizerEndpoint extends PipeServiceGrpc.PipeServiceImplBase {

    @Inject
    Vectorizer vectorizer;

    @Override
    public void send(PipeRequest req, StreamObserver<PipeReply> responseObserver) {
        PipeDocument document = req.getDocument();
        Map<String, Struct> fieldsMap = Maps.newHashMapWithExpectedSize(2);
        fieldsMap.putAll(document.getFieldsMap());
        String body = document.getBody();
        Collection<Float> vector = vectorizer.getEmbeddings(body);
        Struct embeddings = Struct.newBuilder()
                .putFields("vector",
                        Value.newBuilder().setListValue(convert(vector)).build()
                ).build();
        fieldsMap.put("embeddings", embeddings);
        PipeDocument docWithEmbeddings = document.toBuilder().putAllFields(fieldsMap).build();
        PipeReply reply =  PipeReply.newBuilder()
                .setDocument(docWithEmbeddings).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    public static ListValue convert(Collection<Float> floats) {
        ListValue.Builder builder = ListValue.newBuilder();
        for (Float f : floats) {
            double doubleResult = f.doubleValue();
            Value value = Value.newBuilder().setNumberValue(doubleResult).build();
            builder.addValues(value);
        }
        return builder.build();
    }
}
