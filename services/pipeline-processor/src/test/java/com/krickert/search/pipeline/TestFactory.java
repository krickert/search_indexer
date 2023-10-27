package com.krickert.search.pipeline;

import io.micronaut.context.ApplicationContext;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TestFactory {



    @Inject
    private ApplicationContext context;


    @PostConstruct
    void testContext() {
        context.isRunning();
    }


}
