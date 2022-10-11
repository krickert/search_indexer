package com.krickert.search;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Executor;

@Configuration
public class SearchApplicationConfiguration implements AsyncConfigurer {
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("MyExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean
    @Scope("prototype")
    public TokenizerModel getTokenizer() throws IOException {
        InputStream inputStream = resolver.getResource("/models/latest/opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin").getInputStream();
        return new TokenizerModel(inputStream);
    }

    @Bean(name="organizationFinder")
    @Scope("singleton")
    public TokenNameFinderModel getOrganizationFinderModel() throws IOException {
        InputStream modelFile = resolver.getResource("/models/1.5/en-ner-organization.bin").getInputStream();
        return new TokenNameFinderModel(modelFile);
    }

    @Bean(name="locationFinder")
    @Scope("singleton")
    public TokenNameFinderModel getLocationFinderModel() throws IOException {
        InputStream modelFile = resolver.getResource("/models/1.5/en-ner-location.bin").getInputStream();
        return new TokenNameFinderModel(modelFile);
    }

    @Bean(name="personFinder")
    @Scope("prototype")
    public TokenNameFinderModel getPersonFinderModel() throws IOException {
        InputStream modelFile = resolver.getResource("/models/1.5/en-ner-person.bin").getInputStream();
        return new TokenNameFinderModel(modelFile);
    }

    @Bean(name="dateFinder")
    @Scope("singleton")
    public TokenNameFinderModel getDateFinder() throws IOException {
        InputStream modelFile = resolver.getResource("/models/1.5/en-ner-date.bin").getInputStream();
        return new TokenNameFinderModel(modelFile);
    }


}
