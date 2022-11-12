package com.krickert.search.model.test.util;

import com.krickert.search.model.wiki.WikiArticle;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TestDataHelper {
    public final Collection<WikiArticle> fewHunderedArticles = createFewHunderedArticles();

    public Collection<WikiArticle> getFewHunderedArticles() {
        return fewHunderedArticles;
    }

    public static Collection<WikiArticle> createFewHunderedArticles() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("articles");
        assert url != null;
        String path = url.getPath();
        File[] directory = new File(path).listFiles();
        assert directory != null;
        List<File> filesInDir = Arrays.asList(directory);

        List<WikiArticle> returnVal = Lists.newArrayList();
        filesInDir.forEach((file) -> {
            try {
                returnVal.add(
                        WikiArticle.parseFrom(FileUtils.openInputStream(file)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return returnVal;
    }

}
