package com.krickert.search.indexer.enhancers;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class TextChunkerWithPronounResolution {
    private StanfordCoreNLP pipeline;

    public TextChunkerWithPronounResolution() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,coref");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String replaceCoreferencesInSentence(String sentence, Map<Integer, CorefChain> corefChains) {
        String replacedSentence = new String(sentence);
        for (CorefChain chain : corefChains.values()) {
            if (chain.getMentionsInTextualOrder().size() < 2) continue;

            String replace =  chain.getRepresentativeMention().mentionSpan.toLowerCase();

            for(CorefChain.CorefMention mention : chain.getMentionsInTextualOrder()) {
                String mentionText = " " + mention.mentionSpan + " ";
                String replaceText = " " + replace + " ";
                if (replacedSentence.contains(mentionText)) {
                    replacedSentence = replacedSentence.replace(mentionText, replaceText);
                }
            }
        }
        return replacedSentence.trim();
    }

    public List<String> generateChunks(String documentText) {
        Annotation document = new Annotation(documentText);
        this.pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        Map<Integer, CorefChain> corefChains = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);

        List<String> chunks = new ArrayList<>();
        for (CoreMap sentence : sentences) {
            chunks.add(replaceCoreferencesInSentence(sentence.toString(), corefChains));
        }
        return chunks;
    }

}