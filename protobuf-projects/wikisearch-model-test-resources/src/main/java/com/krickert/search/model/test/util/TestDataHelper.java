package com.krickert.search.model.test.util;

import com.google.common.collect.Maps;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.model.wiki.WikiArticle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.FileSystems.newFileSystem;

/**
 * TestDataHelper is a utility class that provides methods for retrieving test data.
 * It contains static fields and static methods for creating and retrieving test data.
 */
public class TestDataHelper {
    /**
     * This variable represents a collection of few hundred WikiArticles.
     */
    private static final Collection<WikiArticle> fewHunderedArticles = createFewHunderedArticles();
    /**
     * Represents a collection of pipe documents.
     *
     * <p>
     * The fewHunderedPipeDocuments variable is a private static final field of the TestDataHelper class. It stores a collection
     * of PipeDocument objects, representing a few hundred pipe documents. The collection is created by invoking the
     * createFewHunderedPipeDocuments method. Each pipe document is associated with a unique identifier and contains
     * relevant information.
     * </p>
     *
     * @see TestDataHelper
     * @see PipeDocument
     * @see #createFewHunderedPipeDocuments()
     */
    private static final Collection<PipeDocument> fewHunderedPipeDocuments = createFewHunderedPipeDocuments();
    /**
     * Map containing a few hundred pipe documents.
     *
     * The key of the map is the ID of the pipe document and the value is the pipe document object.
     */
    private static final Map<String, PipeDocument> fewHunderedPipeDocumentsMap = createPipeDocumentMapById();
    /**
     * A map that stores a few hundred WikiArticle objects with their respective IDs as keys.
     * The map is initialized with data from the createArticleMapById() method.
     */
    private static final Map<String, WikiArticle> fewHunderedArticlesMap = createArticleMapById();

    /**
     * Retrieves a collection of PipeDocuments.
     *
     * @return A collection of PipeDocuments containing several hundred elements.
     */
    public static Collection<PipeDocument> getFewHunderedPipeDocuments() {
        return fewHunderedPipeDocuments;
    }

    /**
     * Retrieves a collection of few hundred WikiArticle objects.
     *
     * @return a Collection of WikiArticle objects.
     */
    public static Collection<WikiArticle> getFewHunderedArticles() {
        return fewHunderedArticles;
    }

    /**
     * Retrieves a map of a few hundred pipe documents.
     *
     * @return the map of pipe documents, where the keys are IDs and the values are PipeDocument objects
     */
    public static Map<String, PipeDocument> getFewHunderedPipeDocumentsMap() {
        return fewHunderedPipeDocumentsMap;
    }

    /**
     * Retrieves a map of few hundred articles.
     *
     * @return A map of few hundred articles, with article IDs as keys and WikiArticle objects as values.
     */
    public static Map<String, WikiArticle> getFewHunderedArticlesMap() {
        return fewHunderedArticlesMap;
    }


    /**
     * Creates a map of PipeDocument objects by their IDs.
     *
     * @return The map of PipeDocument objects, where the keys are IDs and the values are PipeDocument objects.
     */
    private static Map<String, PipeDocument> createPipeDocumentMapById() {
        Collection<PipeDocument> docs = createFewHunderedPipeDocuments();
        Map<String, PipeDocument> returnVal = Maps.newHashMapWithExpectedSize(docs.size());
        docs.forEach((doc) -> returnVal.put(doc.getId(), doc));
        return returnVal;
    }

    /**
     * Creates a map of WikiArticle objects by their IDs.
     *
     * @return A map of WikiArticle objects, where the keys are the IDs of the articles and the values are the corresponding
     * WikiArticle objects.
     */
    private static Map<String, WikiArticle> createArticleMapById() {
        Collection<WikiArticle> docs = createFewHunderedArticles();
        Map<String, WikiArticle> returnVal = Maps.newHashMapWithExpectedSize(docs.size());
        docs.forEach((doc) -> returnVal.put(doc.getId(), doc));
        return returnVal;
    }


    /**
     * Creates a collection of PipeDocuments from a specified directory.
     *
     * @return A collection containing several hundred PipeDocuments.
     */
    private static Collection<PipeDocument> createFewHunderedPipeDocuments() {
        String directory = "/pipe_documents";
        Stream<Path> walk = getPathsFromDirectory(directory);
        List<PipeDocument> returnVal = new ArrayList<>();
        walk.forEach((file) -> {
            try {
                if (!file.getFileName().toString().equals("pipe_documents")) {
                    returnVal.add(
                            PipeDocument.parseFrom(Files.newInputStream(file)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return returnVal;
    }

    /**
     * Retrieves a collection of wiki articles from a given directory.
     *
     * @return A collection of {@link WikiArticle} objects.
     */
    private static Collection<WikiArticle> createFewHunderedArticles() {
        String directory = "/articles";
        Stream<Path> walk = getPathsFromDirectory(directory);
        List<WikiArticle> returnVal = new ArrayList<>();
        walk.forEach((file) -> {
            try {
                if (!file.getFileName().toString().equals("articles")) {
                    returnVal.add(
                            WikiArticle.parseFrom(Files.newInputStream(file)));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return returnVal;
    }

    /**
     * Retrieves a stream of paths from the given directory.
     *
     * @param directory the directory from which to retrieve the paths
     * @return a stream of paths from the directory
     * @throws RuntimeException if any error occurs while retrieving the paths
     */
    private static Stream<Path> getPathsFromDirectory(String directory) {
        URI uri;
        try {
            uri = Objects.requireNonNull(TestDataHelper.class.getResource(directory)).toURI();
        } catch (URISyntaxException | NullPointerException e) {
            throw new RuntimeException(e);
        }
        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = null;
            try {
                fileSystem = newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (FileSystemAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
                myPath = fileSystem.getPath(directory);
            }
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> walk;
        try {
            walk = Files.walk(myPath, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return walk;
    }

}
