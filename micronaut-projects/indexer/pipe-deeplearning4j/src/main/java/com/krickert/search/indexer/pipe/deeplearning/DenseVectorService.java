package com.krickert.search.indexer.pipe.deeplearning;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.deeplearning4j.bagofwords.vectorizer.TextVectorizer;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class DenseVectorService {

    private final Word2Vec model;
    private final WeightLookupTable<VocabWord> lookupTable;
    private final TokenizerFactory tokenizerFactory;



    @Inject
    public DenseVectorService(String pathToGloveModel) {
        Preconditions.checkNotNull(pathToGloveModel);
        this.model =  WordVectorSerializer.readWord2VecModel(pathToGloveModel);
        this.lookupTable = model.getLookupTable();
        this.tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

    }

    private INDArray toVector(List<String> documentAsList) {
        List<INDArray> documentVectors = new ArrayList<>();
        for (String word : documentAsList) {
            INDArray wordVector = lookupTable.vector(word);
            if (wordVector != null) {
                documentVectors.add(wordVector);
            }
        }
        INDArray documentVector = Nd4j.create(lookupTable.layerSize());
        for (INDArray wordVector : documentVectors) {
            documentVector.addi(wordVector);
        }
        return documentVector.divi(documentVectors.size());
    }

    public double[] calculateVector(String text) {
        List<String> cleaned = cleanText(text);

        return toVector(cleaned).toDoubleVector();
    }

    private List<String> cleanText(String text) {
        return Lists.newArrayList(text);
    }


}