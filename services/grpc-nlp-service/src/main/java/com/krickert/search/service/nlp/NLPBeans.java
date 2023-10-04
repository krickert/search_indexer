package com.krickert.search.service.nlp;

import com.google.common.base.Preconditions;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import jakarta.inject.Singleton;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

@Singleton
public class NLPBeans {

    private final NlpExtractor organizationExtractor;
    private final NlpExtractor dateExtractor;
    private final NlpExtractor locationExtractor;
    private final NlpExtractor personExtractor;

    public NLPBeans(@Value("${nlp.en_tokenizer}") String englishTokenizerLocation,
                    @Value("${nlp.ner_org_model}") String nlpNerOrgModel,
                    @Value("${nlp.ner_person_model}") String nlpNerPersonModel,
                    @Value("${nlp.ner_location_model}") String nlpNerLocationModel,
                    @Value("${nlp.ner_date_model}") String nlpNerDateModel)
            throws IOException {

        final ClassPathResourceLoader loader = getClassPathResourceLoader();
        this.organizationExtractor = createNlpExtractor(ServiceType.ORGANIZATION,
                englishTokenizerLocation, nlpNerOrgModel, loader);
        this.personExtractor = createNlpExtractor(ServiceType.PERSON, englishTokenizerLocation, nlpNerPersonModel, loader);
        this.locationExtractor = createNlpExtractor(ServiceType.LOCATION, englishTokenizerLocation, nlpNerLocationModel, loader);
        this.dateExtractor = createNlpExtractor(ServiceType.DATE, englishTokenizerLocation, nlpNerDateModel, loader);
   }

    public NlpExtractor getOrganizationExtractor() {
        return organizationExtractor;
    }

    public NlpExtractor getPersonExtractor() {
        return personExtractor;
    }

    public NlpExtractor getLocationExtractor() {
        return locationExtractor;
    }
    public NlpExtractor getDateExtractor() {
        return dateExtractor;
    }



    private NlpExtractor createNlpExtractor(
            ServiceType serviceType,
            String tokenizerLocation,
            String nlpNerModel,
            ClassPathResourceLoader loader) throws IOException {
        TokenizerModel tokenizerModel = createTokenizerModel(tokenizerLocation, loader);
        Optional<URL> resource = loader.getResource(nlpNerModel);
        if (resource.isEmpty()) {
            throw new RuntimeException("tokenizer file name was set but file is empty or not present.");
        }
        TokenNameFinderModel finderModel = new TokenNameFinderModel(resource.get().openStream());
        return new NlpExtractor(serviceType, tokenizerModel, finderModel);
    }

    private static ClassPathResourceLoader getClassPathResourceLoader() {
        final ClassPathResourceLoader loader;
        Optional<ClassPathResourceLoader> loaderOptional = new ResourceResolver().getLoader(ClassPathResourceLoader.class);
        if (loaderOptional.isEmpty()) {
            throw new RuntimeException("classpath loader couldn't start");
        }
        loader = loaderOptional.get();
        return loader;
    }

    private TokenizerModel createTokenizerModel(String tokenizerLocation,
                                                ClassPathResourceLoader loader) {
        Preconditions.checkNotNull(tokenizerLocation);
        Optional<URL> resource = loader.getResource(tokenizerLocation);
        if (resource.isEmpty()) {
            throw new RuntimeException("tokenizer file name was set but file is empty or not present.");
        }
        try {
            return new TokenizerModel(resource.get().openStream());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create english tokenizer. is the file set right?", e);
        }
    }






}
