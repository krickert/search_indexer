package com.krickert.search.service.nlp;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.Micronaut;

public class NlpNerApplication {
    public static void main(String[] args) {
        Micronaut.run(NlpNerApplication.class, args);
    }



    @ContextConfigurer
    public static class Configurer implements ApplicationContextConfigurer {
        @Override
        public void configure(@NonNull ApplicationContextBuilder builder) {
            builder.eagerInitSingletons(true);
        }
    }
}