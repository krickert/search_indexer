package com.krickert.search.service.nlp.grpc;

import com.google.common.collect.Maps;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;
import com.krickert.search.service.PipeReply;
import com.krickert.search.service.PipeRequest;
import com.krickert.search.service.PipeServiceGrpc;
import com.krickert.search.service.nlp.NLPBeans;
import com.krickert.search.service.nlp.NlpExtractor;
import com.krickert.search.service.nlp.ServiceType;
import io.grpc.stub.StreamObserver;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


@Singleton
public class NLPizerEndpoint extends PipeServiceGrpc.PipeServiceImplBase {

    private final Map<String, NlpExtractor> servicesEnabledMap;
    private static final Logger log = LoggerFactory.getLogger(NLPizerEndpoint.class);

    /**
     * NLPizerEndpoint class is responsible for processing incoming data and applying NLP (Natural Language Processing)
     * techniques to extract entities for enabled services.
     */
    @Inject
    NLPizerEndpoint(
            @io.micronaut.context.annotation.Value("${nlp.services_enabled}")
             Collection<String> servicesEnabled,
            NLPBeans nlpBeans) {
        checkNotNull(nlpBeans);
        checkNotNull(servicesEnabled);
        this.servicesEnabledMap = Maps.newHashMapWithExpectedSize(servicesEnabled.size());
        for(String service : checkNotNull(servicesEnabled)) {
            resolveService(nlpBeans, service);
        }
    }

    private void resolveService(NLPBeans nlpBeans, String service) {
        ServiceType serviceType = ServiceType.lookup(service);
        this.servicesEnabledMap.put(
                serviceType.getServiceName(),
                serviceType.getExtractor(nlpBeans)
        );
    }

    @Override
    public void send(PipeRequest req, StreamObserver<PipeReply> responseObserver) {
        PipeDocument document = req.getDocument();
        final PipeDocument doc;
        if (req.getDocument().getDocumentType().equals("ARTICLE")) {
            doc =  addNlpEntities(document);
        } else {
            doc = req.getDocument();//do nothing!
        }
        PipeReply reply =  PipeReply.newBuilder()
                .setDocument(doc).build();
        log.debug("calling onNext for {}", doc.getTitle());
        responseObserver.onNext(reply);
        log.debug("calling onCompleted for {}", doc.getTitle());
        responseObserver.onCompleted();
    }

    private PipeDocument addNlpEntities(PipeDocument document) {
        PipeDocument.Builder documentWitNlp = document.toBuilder();
        String body = document.getBody();
        //we will take the body and title of each service and "nlpize" the fields
        Struct.Builder nlpEntitiesForGrpc = Struct.newBuilder();
        for (NlpExtractor service : servicesEnabledMap.values()) {
            Collection<String> nlpEntities = service.extract(body);
            if (CollectionUtils.isEmpty(nlpEntities)) {
                continue;//no need to add fields that aren't there
            }
            nlpEntitiesForGrpc.putFields(service.getServiceType().getServiceName(),
                            Value.newBuilder().setListValue(convertStrings(nlpEntities)).build()
                    ).build();
        }
        documentWitNlp.mergeCustomData(nlpEntitiesForGrpc.build());
        return documentWitNlp.build();
    }

    public static ListValue convertStrings(Collection<String> strings) {
        ListValue.Builder builder = ListValue.newBuilder();
        for (String s : strings) {
            Value value = Value.newBuilder().setStringValue(s).build();
            builder.addValues(value);
        }
        return builder.build();
    }
}
