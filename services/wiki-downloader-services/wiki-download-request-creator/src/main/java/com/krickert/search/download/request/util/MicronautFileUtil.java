package com.krickert.search.download.request.util;

import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.file.FileSystemResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Optional;

public class MicronautFileUtil {
    private static final Logger log = LoggerFactory.getLogger(MicronautFileUtil.class);

    public static Optional<String> readFileAsString(String commandlineFileName) {
        try {
            Optional<ClassPathResourceLoader> loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class);
            if (loader.isEmpty()) {
                throw new RuntimeException("Cannot find loader");
            }
            Optional<URL> resource = loader.get().getResource("classpath:" + commandlineFileName);
            if (resource.isPresent()) {
                return Optional.of(IOUtils.toString(resource.get().openStream(), Charset.defaultCharset()));
            }
        } catch (NoSuchElementException | IOException e) {
            log.debug("element not found in classpath", e);
        }
        Optional<FileSystemResourceLoader> loader = new ResourceResolver().getLoader(FileSystemResourceLoader.class);
        if (loader.isEmpty()) {
            throw new RuntimeException("Cannot find loader");
        }
        Optional<URL> resource = loader.get().getResource("file:" + commandlineFileName);
        log.debug("did we find the file? {}", resource.isPresent());
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(IOUtils.toString(resource.get().openStream(), Charset.defaultCharset()));
        } catch (NoSuchElementException | IOException e) {
            log.error("File was specified: [{}] but does not exist in classpath or as a file.", commandlineFileName);
            return Optional.empty();
        }
    }
}
