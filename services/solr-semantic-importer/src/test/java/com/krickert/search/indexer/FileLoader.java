package com.krickert.search.indexer;

import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Singleton
public class FileLoader {
    private final ResourceLoader resourceLoader;

    @Inject
    public FileLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String loadResource(String resourceName) throws IOException {
        Optional<InputStream> file = resourceLoader.getResourceAsStream(resourceName);
        if (file.isPresent()) {
            try (InputStream is = file.get()) {
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            throw new FileNotFoundException(resourceName);
        }
    }

    public byte[] loadResourceBytes(String resourceName) throws IOException {
        Optional<InputStream> file = resourceLoader.getResourceAsStream(resourceName);
        if (file.isPresent()) {
            try (InputStream is = file.get()) {
                return IOUtils.toByteArray(is);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            throw new FileNotFoundException(resourceName);
        }
    }
}
