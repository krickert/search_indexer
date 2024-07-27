package com.krickert.search.indexer.enhancers;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PronounResolutionChunker implements Chunker {
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        // Initiate StanfordCoreNLP with annotation processors
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Input document
        String documentText = "John bought two pencils. He gave one to Jessy.";

        // Create an empty Annotation object
        Annotation document = new Annotation(documentText);

        // Run all annotation processors on the document
        pipeline.annotate(document);

        // Get the list of sentences in the document
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        System.out.println("Chunks:");
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Print out chunks (i.e., tokens along with their POS tags)
                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                System.out.println(word + "/" + pos);
                chunks.add(word + "/" + pos);
            }
            System.out.println();
        }

        // Get the coreference link graph
        // The graph contains a set of mentions which are linked by means of their reference
        Map<Integer, CorefChain> corefChains = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
        System.out.println("Pronoun resolutions:");
        for (Map.Entry<Integer, CorefChain> entry : corefChains.entrySet()) {
            CorefChain chain = entry.getValue();
            // Print the reference (resolved pronoun) and its corresponding mention (the source)
            System.out.println("Reference:" + chain.getRepresentativeMention() + ", Mention: " + chain.getMentionMap());
        }
        return chunks;
    }
}