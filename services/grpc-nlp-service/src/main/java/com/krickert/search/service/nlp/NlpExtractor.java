package com.krickert.search.service.nlp;

import com.google.common.collect.Sets;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.util.Collection;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NlpExtractor {
    final TokenizerModel tokenizer;
    final TokenNameFinderModel nameFinder;
    final ServiceType serviceType;

    public NlpExtractor(ServiceType serviceType,
                        TokenizerModel tokenizerModel,
                        TokenNameFinderModel finderModel) {
        this.tokenizer = checkNotNull(tokenizerModel);
        this.nameFinder = checkNotNull(finderModel);
        this.serviceType = checkNotNull(serviceType);
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public Collection<String> extract(String text) {
        Tokenizer tokenizerME = new TokenizerME(tokenizer);
        String[] tokens = tokenizerME.tokenize(text);
        return extract(tokens);
    }

    public Collection<String> extract(String[] tokens) {
        Set<String> results = Sets.newHashSet();
        NameFinderME nameFinderME = new NameFinderME(this.nameFinder);
        Span[] spans = nameFinderME.find(tokens);
        for (Span span : spans) {
            StringBuffer sb = new StringBuffer();
            for (int i = span.getStart(); i < span.getEnd(); i++) {
                sb.append(tokens[i]).append(" ");
            }
            results.add(sb.toString().trim());
        }
        return results;
    }


}