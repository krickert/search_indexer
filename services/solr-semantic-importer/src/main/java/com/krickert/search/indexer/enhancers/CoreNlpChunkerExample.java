package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.*;
import jakarta.inject.Singleton;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class CoreNlpChunkerExample {

    public Collection<String> chunker(String text) {
        final LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");

        // This is your text source -- it could be a File, a URL, or a String
        Reader reader = new StringReader(text);

        // Start tokenization
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);

        Collection<String> returnVal = new ArrayList<>();
        for (List<HasWord> sentList : dp) {
            Tree parse = lp.apply(sentList);
            List<Word> words = parse.yieldWords();
            StringBuilder sentence = new StringBuilder();
            for (Label word : words) {
                sentence.append(word.value()).append(' ');
            }
            // Do something with your parsed sentence tree
            System.out.println(sentence);
            returnVal.add(parse.toString());
        }
        return returnVal;
    }
}