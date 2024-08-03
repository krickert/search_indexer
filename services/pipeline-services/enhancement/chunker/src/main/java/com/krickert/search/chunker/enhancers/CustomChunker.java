package com.krickert.search.chunker.enhancers;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CustomChunker implements Chunker {
    StanfordCoreNLP pipeline;
    int chunkSize;

    public CustomChunker(int chunkSize) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        this.pipeline = new StanfordCoreNLP(props);
        this.chunkSize = chunkSize;
    }

    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            String chunk = "";
            for (String word: sentence.toString().split("\\s+")) {
                if (chunk.length() + word.length() > chunkSize) {
                    chunks.add(chunk.trim());
                    chunk = "";
                }
                chunk = chunk + " " + word;
            }
            chunks.add(chunk.trim());
        }

        return chunks;
    }

}