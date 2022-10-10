package com.krickert.search.opennlp;

import com.google.common.collect.Sets;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;


@Component
@Scope("prototype")
public class PersonExtractor {

    private final NlpExtractor extractor;

    @Autowired
    public PersonExtractor(TokenizerModel tokenizerModel,
                           @Qualifier("personFinder") TokenNameFinderModel personFinder) {
        this.extractor = new NlpExtractor(tokenizerModel, personFinder);
    }

    public Collection<String> extractPersons(String text) {
        return extractor.extract(text);
    }

    public Collection<String> extractPersons(String[] tokens)  {
        return extractor.extract(tokens);
    }


}
