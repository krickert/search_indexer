package com.krickert.search.service.nlp;

import com.krickert.search.service.nlp.exceptions.UnsupportedServiceException;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The ServiceType enum represents different types of services for NLP Entity Extraction.
 */
public enum ServiceType {
    ORGANIZATION("organization") {
        public NlpExtractor getExtractor(NLPBeans nlpBeans) {
            return nlpBeans.getOrganizationExtractor();
        }
    },
    LOCATION("location") {
        public NlpExtractor getExtractor(NLPBeans nlpBeans) {
            return nlpBeans.getLocationExtractor();
        }
    },
    PERSON("person") {
        public NlpExtractor getExtractor(NLPBeans nlpBeans) {
            return nlpBeans.getPersonExtractor();
        }
    },
    DATE("date") {
        public NlpExtractor getExtractor(NLPBeans nlpBeans) {
            return nlpBeans.getDateExtractor();
        }
    };
    private final String serviceName;

    /**
     * Creates a new ServiceType with the specified service name.
     *
     * @param serviceName the name of the service
     */
    ServiceType(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Looks up a {@link ServiceType} based on the service name.
     *
     * @param service the service name
     * @return the corresponding {@link ServiceType}
     * @throws UnsupportedServiceException if the service name is invalid
     */
    public static ServiceType lookup(String service) {
        checkNotNull(service);
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.getServiceName().equals(service)) {
                return serviceType;
            }
        }
        throw new UnsupportedServiceException("Invalid service name. Valid service names are " + Arrays.toString(ServiceType.values()));
    }
    public abstract NlpExtractor getExtractor(NLPBeans nlpBeans);

    /**
     * Retrieves the name of the service.
     *
     * @return the name of the service
     */
    public String getServiceName() {
        return serviceName;
    }
}