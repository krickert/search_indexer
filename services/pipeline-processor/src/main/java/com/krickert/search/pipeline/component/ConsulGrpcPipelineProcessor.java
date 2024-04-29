package com.krickert.search.pipeline.component;

import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.pipeline.config.PipelineConfig;
import com.krickert.search.pipeline.config.RegisteredPipelines;
import com.krickert.search.pipeline.grpc.ConsulGrpcManagedChannelFactory;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;

/**
 * ConsulGrpcPipelineProcessor is a class that implements the PipelineProcessor interface. It processes a PipeDocument
 * object by sending it to one or more services defined in the PipelineConfig. The class uses a ConsulGrpcManagedChannelFactory
 * to create managed channels for communicating with the services through Consul grpc services registerd with the Grpc PipeService interface.
 */
@Prototype
@Requires(notEnv = Environment.TEST)
public class ConsulGrpcPipelineProcessor implements PipelineProcessor {

    private final PipelineConfig pipelineConfig;
    private final ConsulGrpcManagedChannelFactory consulGrpcManagedChannelFactory;

    /**
     * ConsulGrpcPipelineProcessor is a class that implements the PipelineProcessor interface. It processes a PipeDocument
     * object by sending it to one or more services defined in the PipelineConfig. The class uses a ConsulGrpcManagedChannelFactory
     * to create managed channels for communicating with the services through Consul grpc services registered with the Grpc PipeService interface.
     */
    public ConsulGrpcPipelineProcessor(PipelineConfig pipelineConfig, ConsulGrpcManagedChannelFactory consulGrpcManagedChannelFactory) {
        this.pipelineConfig = pipelineConfig;
        this.consulGrpcManagedChannelFactory = consulGrpcManagedChannelFactory;
    }

    /**
     * This method processes a PipeDocument object by sending it to one or more services defined in the PipelineConfig.
     *
     * @param pipeDocument The PipeDocument object to be processed.
     * @param pipeline     The name of the pipeline to use for processing.
     * @return The processed PipeDocument object.
     */
    @Override
    public PipeDocument process(PipeDocument pipeDocument, String pipeline) {

        RegisteredPipelines registeredPipelines = pipelineConfig.getRegisteredPipelines().get(pipeline);

        for (String service : registeredPipelines.getServices()) {
            pipeDocument = sendDocumentToService(service, pipeDocument);
        }

        return pipeDocument;
    }

    /**
     * Sends a PipeDocument to a service for processing.
     *
     * @param service   The name of the service to send the document to.
     * @param document  The PipeDocument to be sent.
     * @return The PipeDocument returned by the service after processing.
     */
    private PipeDocument sendDocumentToService(String service, PipeDocument document) {
        ManagedChannel managedChannel = consulGrpcManagedChannelFactory.managedChannelFromConsul(service);

        PipeServiceGrpc.PipeServiceBlockingStub stub =
                PipeServiceGrpc.newBlockingStub(managedChannel);

        return stub.send(PipeRequest.newBuilder().setDocument(document).build()).getDocument();
    }
}
