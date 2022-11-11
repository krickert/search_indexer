package com.krickert.search.download.request;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import jakarta.inject.Singleton;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Optional;

public class MicronautFileUtil {
    private static final Logger log = LoggerFactory.getLogger(MicronautFileUtil.class);

    public static Optional<String> readFileAsString(String commandlineFileName) {
        try {
            ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
            Optional<URL> resource = loader.getResource("classpath:" + commandlineFileName);
            if (resource.isPresent()) {
                return Optional.of(FileUtils.readFileToString(new File(resource.get().getFile()), Charset.defaultCharset()));
            }
        } catch (NoSuchElementException | IOException e) {
            log.debug("element not found in classpath", e);
        }
        FileSystemResourceLoader loader = new ResourceResolver().getLoader(FileSystemResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("file:" + commandlineFileName);
        log.debug("did we find the file? {}", resource.isPresent());
        try {
            return Optional.of(FileUtils.readFileToString(new File(resource.get().getFile()), Charset.defaultCharset()));
        } catch (NoSuchElementException | IOException e) {
            log.error("File was specified: [{}] but does not exist in classpath or as a file.", commandlineFileName);
            return Optional.ofNullable(null);
        }
    }
}
