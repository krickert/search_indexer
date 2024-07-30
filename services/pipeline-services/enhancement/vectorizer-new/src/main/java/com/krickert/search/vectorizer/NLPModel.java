package com.krickert.search.vectorizer;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum NLPModel {
    ALL_MINILM_L12_V2("all-MiniLM-L12-v2"),
    E5_BASE_v2("e5-base-v2"),
    MSMARCO_MINILM_L_6_V3("mamarco-MiniLM-L-6-v3"),
    MSMARCO_DISTILBERT_BASE_V4("mamarco-distilbert-base-V4"),
    PARAPHRASE_MULTILINGUAL_MPNET_BASE_V2("paraphrase-multilingual-mpnet-base-V2");

    private String modelName;

    NLPModel(String modelNmae) {
        this.modelName = modelNmae;
    }
}
