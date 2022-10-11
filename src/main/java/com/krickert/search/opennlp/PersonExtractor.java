package com.krickert.search.opennlp;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;

import java.util.Collection;


public class PersonExtractor {

    private final NlpExtractor extractor;

    public PersonExtractor(TokenizerModel tokenizerModel,
                           TokenNameFinderModel personFinder) {
        this.extractor = new NlpExtractor(tokenizerModel, personFinder);
    }

    public Collection<String> extractPersons(String text) {
        return extractor.extract(text);
    }

    public Collection<String> extractPersons(String[] tokens)  {
        return extractor.extract(tokens);
    }


}
