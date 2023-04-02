package com.krickert.search.indexer.pipe.nlp;

import com.google.common.collect.Lists;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

@Prototype
public class SentenceExtractor {

    final SentenceModel sentenceModel;

    public SentenceExtractor() {
        ResourceLoader loader = ClassPathResourceLoader.defaultLoader(SentenceExtractor.class.getClassLoader());
        try (InputStream modelIn = loader.getResourceAsStream("opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin").get()) {
            this.sentenceModel = new SentenceModel(modelIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<String> extractSentences(String text) {
        SentenceDetectorME sentenceDetectorME = new SentenceDetectorME(sentenceModel);
        return Lists.newArrayList(sentenceDetectorME.sentDetect(text));
    }
}
