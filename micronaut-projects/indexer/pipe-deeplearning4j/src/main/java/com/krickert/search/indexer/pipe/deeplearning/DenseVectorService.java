package com.krickert.search.indexer.pipe.deeplearning;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class DenseVectorService {

    private final WeightLookupTable<VocabWord> lookupTable;

    @Inject
    public DenseVectorService(String pathToGloveModel) {
        Preconditions.checkNotNull(pathToGloveModel);
        Word2Vec model = WordVectorSerializer.readWord2VecModel(pathToGloveModel);
        this.lookupTable = model.getLookupTable();
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
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
        try (INDArray documentVector = Nd4j.create(lookupTable.layerSize())) {
            for (INDArray wordVector : documentVectors) {
                documentVector.addi(wordVector);
            }
            return documentVector.divi(documentVectors.size());
        }
    }

    public double[] calculateVector(String text) {
        List<String> cleaned = cleanText(text);

        final double[] doubleVector = toVector(cleaned).toDoubleVector();
        return doubleVector;
    }

    private List<String> cleanText(String text) {
        return Lists.newArrayList(text);
    }


}