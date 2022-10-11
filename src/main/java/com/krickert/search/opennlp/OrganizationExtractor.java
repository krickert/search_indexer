package com.krickert.search.opennlp;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.util.Collection;

public class OrganizationExtractor {

    final NlpExtractor nlpExtractor;

    public OrganizationExtractor(TokenizerModel tokenizerModel,
                                 TokenNameFinderModel orgFinder) {
        this.nlpExtractor = new NlpExtractor(tokenizerModel, orgFinder);
    }

    public Collection<String> extractOrganizations(String text) {
        return nlpExtractor.extract(text);
    }

    public Collection<String> extractOrganizations(String[] text) throws IOException {
        return nlpExtractor.extract(text);
    }
}
