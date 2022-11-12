package com.krickert.search.model.test.util;

import com.krickert.search.model.wiki.WikiArticle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.FileSystems.newFileSystem;

public class TestDataHelper {
    public final Collection<WikiArticle> fewHunderedArticles = createFewHunderedArticles();

    public Collection<WikiArticle> getFewHunderedArticles() {
        return fewHunderedArticles;
    }

    public static Collection<WikiArticle> createFewHunderedArticles() {
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

    public static void main(String args[]) {
        System.out.println(createFewHunderedArticles());
    }

}
