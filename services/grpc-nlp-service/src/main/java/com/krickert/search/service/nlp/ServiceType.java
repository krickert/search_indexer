package com.krickert.search.service.nlp;

import static com.google.common.base.Preconditions.checkNotNull;

public enum ServiceType {
    ORGANIZATION("organization"),
    PERSON("person"),
    LOCATION("location"),
    DATE("date");
    private final String serviceName;

    ServiceType(String serviceName) {
        this.serviceName = serviceName;
    }

    public static ServiceType lookup(String service) {
        checkNotNull(service);
        if (DATE.getServiceName().equals(service)) {
            return DATE;
        } else if (LOCATION.getServiceName().equals(service)) {
            return LOCATION;
        } else if (PERSON.getServiceName().equals(service)) {
            return PERSON;
        } else if (ORGANIZATION.getServiceName().equals(service)) {
            return ORGANIZATION;
        }
        throw new RuntimeException("Invalid service name. Valid service names are" + ServiceType.values());
    }
    public String getServiceName() {
        return serviceName;
    }
}