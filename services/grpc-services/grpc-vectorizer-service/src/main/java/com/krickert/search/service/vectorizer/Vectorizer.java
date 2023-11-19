package com.krickert.search.service.vectorizer;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.google.common.collect.Lists;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Singleton
public class Vectorizer {

    private static final Logger log = LoggerFactory.getLogger(Vectorizer.class);

    final Criteria<String, float[]> criteria;

    final ZooModel<String, float[]> model;

    public Vectorizer() throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls("djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L6-v2")
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .build();

        this.criteria = criteria;
        this.model = criteria.loadModel();
    }

    public float[] embeddings(String text) {
        log.info("vectorizing {}", text);
        try (Predictor<String, float[]> predictor = model.newPredictor()) {
            float[] response = predictor.predict(text);
            log.debug("Text input [{}] returned embeddings [{}]", text, response);
            log.info("completed");
            return response;
        } catch (TranslateException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Float> getEmbeddings(String text) {
        log.debug("getting embeddings for {}", text);
        float[] res = this.embeddings(text);
        final List<Float> response;
        if (res == null) {
            response = Collections.emptyList();
        } else {
            response = Lists.newArrayListWithCapacity(res.length);
            for(float embedding : res) {
                response.add(embedding);
            }
        }
        log.debug("Text input [{}] returned embeddings [{}]", text, response);
        return response;
    }
}

