package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CoreNLPEntityRecognizer implements Chunker {
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        // Setting the NLP properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,depparse,coref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // The text to be processed                
        text = "Apple Inc. is an American multinational technology company headquartered in Cupertino, California.";

        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        // Fetch sentences from the document.
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        // Process each sentence.
        for(CoreMap sentence: sentences) {
            // Fetch the named entity tags and print them.
            for(CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String entity = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                chunks.add(token.word() + " --> " + entity);
            }
        }
        return chunks;
    }
}