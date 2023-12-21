package com.krickert.search.service.nlp;

import com.google.common.collect.Sets;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.util.Collection;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * The NlpExtractor class is responsible for extracting named entities from text using NLP (Natural Language Processing).
 * It uses a tokenizer model and a name finder model for this purpose.
 *
 * The supported service types are specified by the ServiceType enum.
 *
 * Example usage:
 *
 * TokenizerModel tokenizerModel = new TokenizerModel(new FileInputStream("en-token.bin"));
 * TokenNameFinderModel finderModel = new TokenNameFinderModel(new FileInputStream("en-ner-person.bin"));
 * NlpExtractor extractor = new NlpExtractor(ServiceType.PERSON, tokenizerModel, finderModel);
 *
 * String text = "John Doe is a data scientist.";
 * Collection<String> namedEntities = extractor.extract(text);
 * System.out.println(namedEntities);
 *
 * Output: [John Doe]
 */
public final class NlpExtractor {
    /**
     * Represents a TokenizerModel used for tokenizing text.
     */
    private final TokenizerModel tokenizer;
    /**
     * The nameFinder variable holds the model used for named entity recognition.
     * It is a private final variable of type TokenNameFinderModel.
     *
     * This class is part of the NlpExtractor system and is used for extracting named entities from text using NLP (Natural Language Processing).
     *
     * Class: NlpExtractor
     *   - tokenizer
     *   - nameFinder
     *   - serviceType
     *
     * You can use the nameFinder object to perform named entity recognition on text by calling the extract method.
     *
     * Method: extract
     *   - Parameters:
     *     - text: The text to be processed for named entity recognition.
     *   - Return type: Collection of strings containing the extracted named entities.
     *
     * Method: extractNamedEntitiesFromTokens
     *   - Parameters:
     *     - tokens: An array of strings representing the text tokens.
     *   - Return type: Collection of strings containing the extracted named entities.
     *
     * Method: createAndUseTokenizerME
     *   - Parameters:
     *     - text: The text to be tokenized.
     *   - Return type: Array of strings representing the tokens.
     *
     * Method: getEntityFromSpan
     *   - Parameters:
     *     - tokens: An array of strings representing the text tokens.
     *     - span: The span object specifying the start and end indices of the named entity.
     *   - Return type: String containing the named entity.
     *
     * Class: ServiceType (Enum)
     *   - ORGANIZATION: Represents the organization service type.
     *   - PERSON: Represents the person service type.
     *   - LOCATION: Represents the location service type.
     *   - DATE: Represents the date service type.
     *
     * Enum Method: lookup
     *   - Parameters:
     *     - service: The name of the service.
     *   - Return type: The corresponding ServiceType enum value.
     *   - Throws: UnsupportedServiceException if the service name is invalid.
     *
     * Enum Method: getServiceName
     *   - Return type: The name of the service.
     *
     * Class: UnsupportedServiceException
     *   - Extends: RuntimeException
     *   - Constructor: Creates a new UnsupportedServiceException with the specified reason.
     */
    private final TokenNameFinderModel nameFinder;
    /**
     * The serviceType variable represents the type of service for NLP Entity Extraction.
     *
     * @see ServiceType
     */
    private final ServiceType serviceType;

    /**
     * Initializes a new instance of the NlpExtractor class.
     *
     * @param serviceType    The service type to be used by the NlpExtractor.
     * @param tokenizerModel The tokenizer model to be used by the NlpExtractor.
     * @param finderModel    The finder model to be used by the NlpExtractor.
     */
    public NlpExtractor(ServiceType serviceType,
                        TokenizerModel tokenizerModel,
                        TokenNameFinderModel finderModel) {
        this.tokenizer = requireNonNull(tokenizerModel);
        this.nameFinder = requireNonNull(finderModel);
        this.serviceType = requireNonNull(serviceType);
    }

    /**
     * Retrieves the service type of the NlpExtractor.
     *
     * @return the service type of the NlpExtractor
     */
   public ServiceType getServiceType() {
        return serviceType;
    }

    /**
     * Extracts named entities from the given text using NLP (Natural Language Processing).
     *
     * @param text The text from which to extract named entities.
     * @return A collection of strings representing the named entities found in the text.
     */
    public Collection<String> extract(String text) {
        String[] tokens = createAndUseTokenizerME(text);
        return extractNamedEntitiesFromTokens(tokens);
    }

    /**
     * Creates a TokenizerME instance and uses it to tokenize the given text.
     *
     * @param text the text to be tokenized
     * @return an array of tokens
     */
    private String[] createAndUseTokenizerME(String text) {
        Tokenizer tokenizerME = new TokenizerME(tokenizer);
        return tokenizerME.tokenize(text);
    }

    /**
     * Extracts named entities from an array of tokens using the provided name finder model.
     *
     * @param tokens the array of tokens
     * @return a collection of named entities extracted from the tokens
     */
    public Collection<String> extractNamedEntitiesFromTokens(String[] tokens) {
        Set<String> results = Sets.newHashSet();
        NameFinderME nameFinderME = new NameFinderME(this.nameFinder);
        Span[] spans = nameFinderME.find(tokens);
        for (Span span : spans) {
            results.add(getEntityFromSpan(tokens, span));
        }
        return results;
    }

    /**
     * Retrieves the entity from the given span of tokens.
     *
     * @param tokens the array of tokens
     * @param span   the span representing the entity
     * @return the entity string extracted from the span
     */
    private String getEntityFromSpan(String[] tokens, Span span) {
        StringBuilder entityBuilder = new StringBuilder();
        for (int i = span.getStart(); i < span.getEnd(); i++) {
            entityBuilder.append(tokens[i]).append(" ");
        }
        return entityBuilder.toString().trim();
    }


}