package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class CoreNLPChunker {
    public static void main(String[] args) {
        // Creating a StanfordCoreNLP object and perfroming Natural language processing steps.
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Input text
        String text = "The quick brown fox jumps over the lazy dog.";

        // Creating an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // Annotating the text
        pipeline.annotate(document);

        // Getting the sentences in the text
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for(CoreMap sentence : sentences) {
            for (String part : sentence.toString().split("\\s+")) {
                System.out.println(part.split("-")[0]);
            }
        }
    }
}