package com.krickert.search.pipeline;

import com.krickert.search.grpc.ConsulGrpcManagedChannelFactory;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import io.grpc.ManagedChannel;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;

@Singleton
@Requires(notEnv = Environment.TEST)
public class ConsulGrpcPipelineProcessor implements PipelineProcessor {

    private final PipelineConfig pipelineConfig;
    private final ConsulGrpcManagedChannelFactory consulGrpcManagedChannelFactory;

    public ConsulGrpcPipelineProcessor(PipelineConfig pipelineConfig, ConsulGrpcManagedChannelFactory consulGrpcManagedChannelFactory) {
        this.pipelineConfig = pipelineConfig;
        this.consulGrpcManagedChannelFactory = consulGrpcManagedChannelFactory;
    }

    @Override
    public PipeDocument process(PipeDocument pipeDocument, String pipeline) {
        RegisteredPipelines config = pipelineConfig.getRegisteredPipelines().get(pipeline);
        PipeDocument currentDoc = pipeDocument;
        for (String service : config.getServices()) {
            ManagedChannel managedChannel = consulGrpcManagedChannelFactory.managedChannelFromConsul(service);
            PipeServiceGrpc.PipeServiceBlockingStub stub =
                    PipeServiceGrpc.newBlockingStub(managedChannel);
            currentDoc = stub.send(PipeRequest.newBuilder().setDocument(currentDoc).build()).getDocument();
        }
        return currentDoc;
    }
}
