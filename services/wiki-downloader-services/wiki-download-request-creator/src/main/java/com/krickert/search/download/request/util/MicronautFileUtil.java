package com.krickert.search.download.request.util;

import io.micronaut.core.io.ResourceLoader;
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

/**
 * Utility class for reading files in Micronaut applications.
 */
public class MicronautFileUtil {
    private static final Logger log = LoggerFactory.getLogger(MicronautFileUtil.class);

    /**
     * Reads the content of a file as a string.
     *
     * @param commandlineFileName The name of the file to read.
     * @return An {@link Optional} containing the file content as a string, or an empty {@link Optional} if the file cannot be found or read.
     */
    public static Optional<String> readFileAsString(String commandlineFileName) {
        Optional<String> classPathContent = readContentFromClassPath(commandlineFileName);
        if (classPathContent.isPresent()) {
            return classPathContent;
        }

        return readContentFromFileSystem(commandlineFileName);
    }

    /**
     * Utility class for reading files in Micronaut applications.
     */
    private static Optional<String> readContentFromClassPath(String fileName) {
        return readContent(ClassPathResourceLoader.class, "classpath:", fileName);
    }

    /**
     * Reads the content of a file from the file system.
     *
     * @param fileName The name of the file to read.
     * @return An {@link Optional} containing the file content as a string,
     *         or an empty {@link Optional} if the file cannot be found or read.
     */
    private static Optional<String> readContentFromFileSystem(String fileName) {
        return readContent(FileSystemResourceLoader.class, "file:", fileName);
    }

    /**
     * Reads the content of a resource using a specified resource loader.
     *
     * @param <T>        The type of resource loader.
     * @param loaderType The class representing the resource loader.
     * @param protocol   The protocol prefix for the resource.
     * @param fileName   The name of the resource.
     * @return An Optional containing the content of the resource as a string, or an empty Optional if the resource cannot be found or read.
     */
    private static <T extends ResourceLoader> Optional<String> readContent(Class<T> loaderType, String protocol, String fileName) {
        try {
            Optional<T> loader = new ResourceResolver().getLoader(loaderType);
            if (loader.isEmpty()) {
                throw new RuntimeException("Cannot find loader");
            }
            Optional<URL> resource = loader.get().getResource(protocol + fileName);
            if (resource.isPresent()) {
                return Optional.of(IOUtils.toString(resource.get().openStream(), Charset.defaultCharset()));
            }
        } catch (NoSuchElementException | IOException e) {
            log.error("File was specified: [{}] but does not exist as {}.", fileName, protocol);
        }
        return Optional.empty();
    }
}
