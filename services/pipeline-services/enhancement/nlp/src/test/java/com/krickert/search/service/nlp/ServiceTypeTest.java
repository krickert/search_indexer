package com.krickert.search.service.nlp;

import com.krickert.search.service.nlp.exceptions.UnsupportedServiceException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ServiceTypeTest class is used to test methods of ServiceType class.
 */
public class ServiceTypeTest {

    /**
     * This test is for getServiceName method of ServiceType class for ORGANIZATION service.
     */
    @Test
    public void testGetServiceNameForOrganization() {
        ServiceType serviceType = ServiceType.ORGANIZATION;
        String expectedServiceName = "organization";
        assertEquals(expectedServiceName, serviceType.getServiceName());
    }

    /**
     * This test is for getServiceName method of ServiceType class for PERSON service.
     */
    @Test
    public void testGetServiceNameForPerson() {
        ServiceType serviceType = ServiceType.PERSON;
        String expectedServiceName = "person";
        assertEquals(expectedServiceName, serviceType.getServiceName());
    }

    /**
     * This test is for getServiceName method of ServiceType class for LOCATION service.
     */
    @Test
    public void testGetServiceNameForLocation() {
        ServiceType serviceType = ServiceType.LOCATION;
        String expectedServiceName = "location";
        assertEquals(expectedServiceName, serviceType.getServiceName());
    }

    /**
     * This test is for getServiceName method of ServiceType class for DATE service.
     */
    @Test
    public void testGetServiceNameForDate() {
        ServiceType serviceType = ServiceType.DATE;
        String expectedServiceName = "date";
        assertEquals(expectedServiceName, serviceType.getServiceName());
    }

    /**
     * This test is for invalid service type.
     */
    @Test
    public void testInvalidServiceType() {
        String invalidServiceType = "invalid";
        UnsupportedServiceException exception = assertThrows(UnsupportedServiceException.class, () -> ServiceType.lookup(invalidServiceType));
        assertEquals("Invalid service name. Valid service names are " + Arrays.toString(ServiceType.values()), exception.getMessage());
    }
}