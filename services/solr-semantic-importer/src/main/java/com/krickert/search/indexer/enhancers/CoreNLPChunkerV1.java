package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class CoreNLPChunkerV1 implements Chunker {
    public List<String> chunk(String text) {
        // Creating a StanfordCoreNLP object and perfroming Natural language processing steps.
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Input text
        text = "The quick brown fox jumps over the lazy dog.";

        // Creating an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // Annotating the text
        pipeline.annotate(document);

        // Getting the sentences in the text
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        List<String> chunks = new ArrayList<>();
        for(CoreMap sentence : sentences) {
            for (String part : sentence.toString().split("\\s+")) {
                chunks.add(part.split("-")[0]);
            }
        }
        return chunks;
    }
}