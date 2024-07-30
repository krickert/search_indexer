package com.krickert.search.indexer.enhancers;

import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

@Singleton
public class ProtobufToSolrSchema {
    private static final Logger log = LoggerFactory.getLogger(ProtobufToSolrSchema.class);

    public Document convertProtobufToSchemaDefinition(Message protobuf) throws InvalidProtocolBufferException {
        log.debug(JsonFormat.printer().print(protobuf));
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("fields");
            doc.appendChild(rootElement);

            addFieldsToSolrSchema(protobuf, rootElement, "");

            return doc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addFieldsToSolrSchema(Message message, Element schemaElement, String prefix) {
        Map<Descriptors.FieldDescriptor, Object> allFields = message.getAllFields();
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : allFields.entrySet()) {
            handleField(schemaElement, prefix, entry);
        }
    }

    private void handleField(Element schemaElement, String prefix, Map.Entry<Descriptors.FieldDescriptor, Object> entry) {
        String fieldName = prefix.isEmpty() ? entry.getKey().getName() : prefix + "_" + entry.getKey().getName();
        if (entry.getValue() instanceof Message) {
            handleMessageField(schemaElement, entry, fieldName);
        } else if (entry.getKey().isMapField()) {
            handleMapField(schemaElement, entry, fieldName);
        } else if (entry.getKey().isRepeated()) {
            handleRepeatedField(schemaElement, entry, fieldName);
        } else {
            addFieldElement(schemaElement, fieldName, entry.getKey().getJavaType().name());
        }
    }

    private void handleMessageField(Element schemaElement, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        if (entry.getValue() instanceof Struct) {
            // Nested Struct types handled separately
        } else if (entry.getValue() instanceof Timestamp) {
            addFieldElement(schemaElement, fieldName, "Date");
        } else if (entry.getValue() instanceof Duration) {
            addFieldElement(schemaElement, fieldName, "String"); // as Duration string
        } else if (entry.getValue() instanceof BytesValue) {
            addFieldElement(schemaElement, fieldName, "Binary");
        } else if (entry.getValue() instanceof StringValue) {
            addFieldElement(schemaElement, fieldName, "String");
        } else {
            addFieldsToSolrSchema((Message) entry.getValue(), schemaElement, fieldName);
        }
    }

    private static void handleRepeatedField(Element schemaElement, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        addFieldElement(schemaElement, fieldName, entry.getKey().getJavaType().name());
    }

    private static void handleMapField(Element schemaElement, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        addFieldElement(schemaElement, fieldName, entry.getKey().getJavaType().name());
    }

    private static void addFieldElement(Element root, String name, String solrClass) {
        Document doc = root.getOwnerDocument();
        Element fieldType = doc.createElement("fieldType");
        fieldType.setAttribute("name", name);
        fieldType.setAttribute("class", solrClass);
        root.appendChild(fieldType);
    }
}