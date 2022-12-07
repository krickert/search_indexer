package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.stub.StreamObserver;
import jakarta.inject.Singleton;

import java.util.Collection;

@Singleton
public class NlpPipeService extends PipeServiceGrpc.PipeServiceImplBase {

    private final SentenceExtractor sentenceExtractor;

    public NlpPipeService(SentenceExtractor sentenceExtractor) {
        super();
        this.sentenceExtractor = sentenceExtractor;
    }

    @Override
    public void send(PipeRequest request, StreamObserver<PipeReply> pipeObserver) {
        Collection<String> sentences = sentenceExtractor.extractSentences(request.getDocument().getBody());
        PipeDocument origDoc = request.getDocument();
        PipeDocument documentReply =
                PipeDocument.newBuilder(origDoc)
                        .addAllSentences(sentences).build();
        PipeReply reply = PipeReply.newBuilder()
                .setDocument(documentReply)
                .setMessage("added document NLP sentence extraction")
                .build();
        pipeObserver.onNext(reply);
        pipeObserver.onCompleted();
    }


}
