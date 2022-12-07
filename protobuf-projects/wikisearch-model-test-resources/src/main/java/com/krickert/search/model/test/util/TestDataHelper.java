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

public class TestDataHelper {
    private static final Collection<WikiArticle> fewHunderedArticles = createFewHunderedArticles();
    private static final Collection<PipeDocument> fewHunderedPipeDocuments = createFewHunderedPipeDocuments();
    private static final Map<String, PipeDocument> fewHunderedPipeDocumentsMap = createPipeDocumentMapById();
    private static final Map<String, WikiArticle> fewHunderedArticlesMap = createArticleMapById();
    public static Collection<PipeDocument> getFewHunderedPipeDocuments() { return fewHunderedPipeDocuments; }
    public static Collection<WikiArticle> getFewHunderedArticles() { return fewHunderedArticles; }
    public static Map<String, PipeDocument> getFewHunderedPipeDocumentsMap() { return fewHunderedPipeDocumentsMap; }
    public static Map<String, WikiArticle> getFewHunderedArticlesMap() { return fewHunderedArticlesMap; }


    private static Map<String, PipeDocument> createPipeDocumentMapById() {
        Collection<PipeDocument> docs = createFewHunderedPipeDocuments();
        Map<String, PipeDocument> returnVal = Maps.newHashMapWithExpectedSize(docs.size());
        docs.forEach((doc) -> returnVal.put(doc.getId(), doc));
        return returnVal;
    }

    private static Map<String, WikiArticle> createArticleMapById() {
        Collection<WikiArticle> docs = createFewHunderedArticles();
        Map<String, WikiArticle> returnVal = Maps.newHashMapWithExpectedSize(docs.size());
        docs.forEach((doc) -> returnVal.put(doc.getId(), doc));
        return returnVal;
    }


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

    private static Stream<Path> getPathsFromDirectory(String directory) {
        URI uri = null;
        try {
            uri = TestDataHelper.class.getResource(directory).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem = null;
            try {
                fileSystem = newFileSystem(uri, Collections.<String, Object>emptyMap());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (FileSystemAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
            }
            myPath = fileSystem.getPath(directory);
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> walk = null;
        try {
            walk = Files.walk(myPath, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return walk;
    }

}
