package com.krickert.search.opennlp;

import com.google.common.collect.Sets;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.util.Collection;
import java.util.Set;

public final class NlpExtractor {
    final Tokenizer tokenizer;
    final NameFinderME nameFinderME;

    public NlpExtractor(TokenizerModel tokenizerModel,
                        TokenNameFinderModel finderModel) {
        this.tokenizer = new TokenizerME(tokenizerModel);
        this.nameFinderME = new NameFinderME(finderModel);
    }

    public Collection<String> extract(String text) {
        String[] tokens = tokenizer.tokenize(text);
        return extract(tokens);
    }

    public Collection<String> extract(String[] tokens) {
        Set<String> results = Sets.newHashSet();
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
