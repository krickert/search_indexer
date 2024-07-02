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

/**
 * NLPBeans is a singleton class that provides access to various NLP (Natural Language Processing) extractors.
 * It creates the NLP extractors using the provided configuration and resource files.
 */
@Singleton
public class NLPBeans {

    /**
     * The organizationExtractor is an instance of the NlpExtractor class and represents a named entity
     * extractor specifically designed for extracting organization names from text using NLP (Natural Language Processing).
     * It utilizes a tokenizer model and a name finder model for this purpose.
     * <p>
     * The organizationExtractor is used in the NLPBeans class to extract organization names from text.
     * <p>
     * Example usage:
     * <p>
     * NlpExtractor organizationExtractor = new NlpExtractor(ServiceType.ORGANIZATION, tokenizerModel, nameFinderModel);
     * <p>
     * String text = "Apple Inc. is a technology company.";
     * Collection<String> organizationNames = organizationExtractor.extract(text);
     * System.out.println(organizationNames);
     * <p>
     * Output: [Apple Inc.]
     */
    private final NlpExtractor organizationExtractor;
    /**
     * The dateExtractor variable is an instance of the NlpExtractor class that is responsible for extracting named entities of type date from text using NLP (Natural Language Processing
     *).
     * <p>
     * Example usage:
     * NlpExtractor dateExtractor = new NlpExtractor(ServiceType.DATE, tokenizerModel, finderModel);
     * <p>
     * String text = "I have a meeting tomorrow at 2pm.";
     * Collection<String> dates = dateExtractor.extract(text);
     * System.out.println(dates);
     * <p>
     * Output: [tomorrow]
     */
    private final NlpExtractor dateExtractor;
    /**
     * The locationExtractor variable is an instance of the NlpExtractor class.
     * It is used for extracting named entities of type location from text using NLP (Natural Language Processing).
     *
     * Example usage:
     *
     * NlpExtractor locationExtractor = new NlpExtractor(ServiceType.LOCATION, tokenizerModel, locationModel);
     *
     * String text = "New York is a city in the United States.";
     * Collection<String> locations = locationExtractor.extract(text);
     * System.out.println(locations);
     *
     * Output: [New York, United States]
     */
    private final NlpExtractor locationExtractor;
    /**
     * private final NlpExtractor personExtractor
     * <p>
     * Description:
     * The `personExtractor` variable represents a specific instance of the `NlpExtractor` class.
     * It is used for extracting named entities of type "person" from text using Natural Language Processing (NLP).
     * <p>
     * Related Classes:
     * - NlpExtractor: The class responsible for extracting named entities from text using NLP.
     */
    private final NlpExtractor personExtractor;

    /**
     * The NLPBeans class is responsible for initializing the NLP extractors for different services.
     *
     * @param englishTokenizerLocation the location of the English tokenizer model file
     * @param nlpNerOrgModel the location of the NER org model file
     * @param nlpNerPersonModel the location of the NER person model file
     * @param nlpNerLocationModel the location of the NER location model file
     * @param nlpNerDateModel the location of the NER date model file
     * @throws IOException if there is an error loading the tokenizer or NER models
     */
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

    /**
     * Retrieves the organization extractor used by the NLPBeans class.
     *
     * @return The organization extractor.
     */
    public NlpExtractor getOrganizationExtractor() {
        return organizationExtractor;
    }

    /**
     * Returns the NlpExtractor instance for extracting person entities.
     *
     * @return The NlpExtractor instance for extracting person entities.
     */
    public NlpExtractor getPersonExtractor() {
        return personExtractor;
    }

    /**
     * Returns the location extractor of the NLPBeans class.
     *
     * @return The location extractor instance of the NLPBeans class.
     */
    public NlpExtractor getLocationExtractor() {
        return locationExtractor;
    }
    /**
     * Retrieves the date extractor NlpExtractor instance.
     *
     * @return The date extractor NlpExtractor instance.
     */
    public NlpExtractor getDateExtractor() {
        return dateExtractor;
    }


    /**
     * Creates a new NlpExtractor instance with the specified parameters.
     *
     * @param serviceType       The service type to be used by the NlpExtractor.
     * @param tokenizerLocation The location of the tokenizer model file.
     * @param nlpNerModel       The location of the name finder model file.
     * @param loader            The ClassPathResourceLoader used for loading the tokenizer and name finder models.
     * @return A new NlpExtractor instance.
     * @throws IOException if there is an error loading the tokenizer or name finder models.
     */
    private NlpExtractor createNlpExtractor(
            ServiceType serviceType,
            String tokenizerLocation,
            String nlpNerModel,
            ClassPathResourceLoader loader) throws IOException {
        TokenizerModel tokenizerModel = createTokenizerModel(tokenizerLocation, loader);
        Optional<URL> resource = loader.getResource(nlpNerModel);
        if (resource.isEmpty()) {
            throw new RuntimeException("File: " + nlpNerModel + " was set but file is empty or not present.");
        }
        TokenNameFinderModel finderModel = new TokenNameFinderModel(resource.get().openStream());
        return new NlpExtractor(serviceType, tokenizerModel, finderModel);
    }

    /**
     * Returns an instance of ClassPathResourceLoader used for loading resources from the classpath.
     *
     * @return An instance of ClassPathResourceLoader.
     * @throws RuntimeException if the classpath loader couldn't start.
     */
    private static ClassPathResourceLoader getClassPathResourceLoader() {
        final ClassPathResourceLoader loader;
        Optional<ClassPathResourceLoader> loaderOptional = new ResourceResolver().getLoader(ClassPathResourceLoader.class);
        if (loaderOptional.isEmpty()) {
            throw new RuntimeException("classpath loader couldn't start");
        }
        loader = loaderOptional.get();
        return loader;
    }

    /**
     * Creates a new TokenizerModel instance with the specified tokenizer location and
     * class path resource loader.
     *
     * @param tokenizerLocation The location of the tokenizer model file.
     * @param loader            The ClassPathResourceLoader used for loading the tokenizer model.
     * @return A new TokenizerModel instance.
     * @throws RuntimeException if the tokenizer file is empty or not present, or if there is an error
     *                          creating the tokenizer model.
     */
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
