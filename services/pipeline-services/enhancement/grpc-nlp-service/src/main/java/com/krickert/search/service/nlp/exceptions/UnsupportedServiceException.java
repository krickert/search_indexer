package com.krickert.search.service.nlp.exceptions;

public class UnsupportedServiceException extends RuntimeException {
    public UnsupportedServiceException(String reason) {
        super(reason);
    }
}
