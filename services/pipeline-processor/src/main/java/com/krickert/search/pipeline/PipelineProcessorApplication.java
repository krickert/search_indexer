package com.krickert.search.pipeline;

import io.micronaut.runtime.Micronaut;

/**
 * The PipelineProcessorApplication class serves as the entry point for running the Pipeline Processor application.
 * It starts the Micronaut framework and initializes the application.
 * <p>
 * To execute the application, run the main method of this class.
 * <p>
 * Example usage:
 * ```java
 * public static void main(String[] args) {
 *     Micronaut.run(PipelineProcessorApplication.class, args);
 * }
 * ```
 */
public class PipelineProcessorApplication {

    public static void main(String[] args) {
        Micronaut.run(PipelineProcessorApplication.class, args);
    }
}