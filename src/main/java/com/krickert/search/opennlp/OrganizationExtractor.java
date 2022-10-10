package com.krickert.search.opennlp;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Scope("prototype")
public class OrganizationExtractor {

    final NlpExtractor nlpExtractor;

    @Autowired
    public OrganizationExtractor(TokenizerModel tokenizerModel,
                                 @Qualifier("organizationFinder") TokenNameFinderModel orgFinder) {
        this.nlpExtractor = new NlpExtractor(tokenizerModel, orgFinder);
    }

    public Collection<String> extractOrganizations(String text) throws IOException {
        return nlpExtractor.extract(text);
    }

    public Collection<String> extractOrganizations(String[] text) throws IOException {
        return nlpExtractor.extract(text);
    }
}
