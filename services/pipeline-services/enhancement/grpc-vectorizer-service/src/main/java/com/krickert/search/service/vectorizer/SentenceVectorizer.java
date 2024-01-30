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



/**
 * The Vectorizer class is responsible for converting text inputs into vector embeddings
 * using a pre-trained model.
 */
@Singleton
public class SentenceVectorizer implements Vectorizer {

    private static final Logger log = LoggerFactory.getLogger(SentenceVectorizer.class);
    private static final String MODEL_URL = "djl://ai.djl.huggingface.pytorch/sentence-transformers/all-MiniLM-L12-v2";

    final Criteria<String, float[]> criteria;
    final ZooModel<String, float[]> model;

    /**
     * The Vectorizer class is responsible for converting text inputs into vector embeddings
     * using a pre-trained model.
     */
    public SentenceVectorizer() throws ModelNotFoundException, MalformedModelException, IOException {
        this.criteria = makeCriteria();
        this.model = this.criteria.loadModel();
    }

    /**
     * Builds a Criteria object for text embedding translation.
     * @return The Criteria object for text embedding translation.
     */
    private Criteria<String, float[]> makeCriteria() {
        return Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls(MODEL_URL)
                .optEngine("PyTorch")
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .build();
    }


    /**
     * Generates vector embeddings for the given text using a pre-trained model.
     *
     * @param text The input text to be vectorized.
     * @return An array of floating-point values representing the embeddings.
     * @throws RuntimeException if an error occurs during embedding translation.
     */
    @Override
    public float[] embeddings(String text) {
        log.debug("vectorizing {}", text);
        try (Predictor<String, float[]> predictor = model.newPredictor()) {
            float[] response = predictor.predict(text);
            log.debug("Text input [{}] returned embeddings [{}]", text, response);
            return response;
        } catch (TranslateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves vector embeddings for the given text.
     *
     * @param text The input text to be vectorized.
     * @return A collection of floating-point values representing the embeddings.
     */
    @Override
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

