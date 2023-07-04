package com.krickert.search.indexer.pipe.nlp;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import jakarta.inject.Singleton;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.krickert.search.model.util.ProtobufUtils.createListValueFromCollection;

@Singleton
public class NlpServiceImpl implements NlpService {

    private final SentenceExtractor sentenceExtractor;

    public NlpServiceImpl(SentenceExtractor sentenceExtractor) {
        this.sentenceExtractor = checkNotNull(sentenceExtractor);
    }

    @Override
    public PipeReply processNlpService(PipeRequest request) {
        checkNotNull(request);
        Collection<String> sentences = sentenceExtractor.extractSentences(request.getDocument().getBody());
        PipeDocument origDoc = request.getDocument();
        PipeDocument documentReply =
                PipeDocument.newBuilder(origDoc)
                        .putListFields("sentences", createListValueFromCollection(sentences))
                        .build();
        return PipeReply.newBuilder()
                .setDocument(documentReply)
                .setMessage("added document NLP sentence extraction")
                .build();
    }
}
