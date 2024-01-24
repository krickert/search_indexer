package com.krickert.search.service.nlp;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.Micronaut;

/**
 * The NlpNerApplication class is the main class of the NLP NER application.
 * It initializes and runs the application using the Micronaut framework.
 */
public class NlpNerApplication {
    public static void main(String[] args) {
        Micronaut.run(NlpNerApplication.class, args);
    }

    /**
     * The Configurer class is a context configurer that implements the ApplicationContextConfigurer interface.
     * It is used to configure the application context builder by setting the eagerInitSingletons property to true.
     *
     * Usage:
     * The Configurer class should be defined as a static class within the main application class.
     * The configure() method should be implemented to apply the desired configuration to the ApplicationContextBuilder.
     * Once the Configurer is defined, it can be registered as a context configurer using the @ContextConfigurer annotation.
     *
     * Example:
     *
     * ```java
     * public class NlpNerApplication {
     *     public static void main(String[] args) {
     *         Micronaut.run(NlpNerApplication.class, args);
     *     }
     *
     *     @ContextConfigurer
     *     public static class Configurer implements ApplicationContextConfigurer {
     *         @Override
     *         public void configure(@NonNull ApplicationContextBuilder builder) {
     *             builder.eagerInitSingletons(true);
     *         }
     *     }
     * }
     * ```
     *
     * In the example above, the Configurer class sets the eagerInitSingletons property of the ApplicationContextBuilder to true,
     * ensuring that all singletons are eagerly initialized during application startup.
     */
    @ContextConfigurer
    public static class Configurer implements ApplicationContextConfigurer {
        @Override
        public void configure(@NonNull ApplicationContextBuilder builder) {
            builder.eagerInitSingletons(true);
        }
    }
}