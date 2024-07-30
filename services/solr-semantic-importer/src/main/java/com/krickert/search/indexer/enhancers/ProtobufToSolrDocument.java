package com.krickert.search.indexer.enhancers;
import com.google.protobuf.*;
import com.google.protobuf.util.JsonFormat;
import jakarta.inject.Singleton;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class ProtobufToSolrDocument {
    private static final Logger log = LoggerFactory.getLogger(ProtobufToSolrDocument.class);

    public SolrInputDocument convertProtobufToSolrDocument(Message protobuf) {
        try {
            log.debug(JsonFormat.printer().print(protobuf));
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        SolrInputDocument solrDoc = new SolrInputDocument();
        addFieldsToSolrDoc(protobuf, solrDoc, "");
        return solrDoc;
    }

    private void addFieldsToSolrDoc(Message message, SolrInputDocument solrDoc, String prefix) {
        Map<Descriptors.FieldDescriptor, Object> allFields = message.getAllFields();
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : allFields.entrySet()) {
            handleField(solrDoc, prefix, entry);
        }
    }

    private void handleField(SolrInputDocument solrDoc, String prefix, Map.Entry<Descriptors.FieldDescriptor, Object> entry) {
        String fieldName = prefix.isEmpty() ? entry.getKey().getName() : prefix + "_" + entry.getKey().getName();
        if (entry.getValue() instanceof Message) {
            handleMessageField(solrDoc, entry, fieldName);
        } else if (entry.getKey().isMapField()) {
            handleMapField(solrDoc, entry, fieldName);
        } else if (entry.getKey().isRepeated()) {
            handleRepeatedField(solrDoc, entry, fieldName);
        } else {
            solrDoc.addField(fieldName, entry.getValue());
        }
    }

    private void handleMessageField(SolrInputDocument solrDoc, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        if (entry.getValue() instanceof Struct) {
            extractFieldsFromStruct((Struct) entry.getValue(), solrDoc, fieldName);
        } else if (entry.getValue() instanceof Timestamp timestamp) {
            handleTimestampType(solrDoc, timestamp, fieldName);
        } else if (entry.getValue() instanceof Duration duration) {
            handleDurationType(solrDoc, duration, fieldName);
        } else if (entry.getValue() instanceof BytesValue bytesValue) {
            handleBytesType(solrDoc, bytesValue, fieldName);
        } else if (entry.getValue() instanceof FloatValue floatValue) {
            handleFloatType(solrDoc, floatValue, fieldName);
        } else if (entry.getValue() instanceof Empty) {
            handleEmptyType(solrDoc, fieldName);
        } else if (entry.getValue() instanceof FieldMask fieldMask) {
            handleFieldMaskType(solrDoc, fieldMask, fieldName);
        } else  {
            addFieldsToSolrDoc((Message) entry.getValue(), solrDoc, fieldName);
        }
    }

    private static void handleRepeatedField(SolrInputDocument solrDoc, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        @SuppressWarnings("unchecked") List<Object> listValue = (List<Object>) entry.getValue();
        for (Object item : listValue) {
            solrDoc.addField(fieldName, item);
        }
    }

    private static void handleMapField(SolrInputDocument solrDoc, Map.Entry<Descriptors.FieldDescriptor, Object> entry, String fieldName) {
        @SuppressWarnings("unchecked") Map<Object, Object> mapValue = (Map<Object, Object>) entry.getValue();
        for (Map.Entry<Object, Object> mapEntry : mapValue.entrySet()) {
            solrDoc.addField(fieldName + "_" + mapEntry.getKey(), mapEntry.getValue());
        }
    }

    private static void handleFieldMaskType(SolrInputDocument solrDoc, FieldMask fieldMask, String fieldName) {
        // Convert paths in FieldMask to a comma-separated string
        String paths = String.join(", ", fieldMask.getPathsList());
        solrDoc.addField(fieldName, paths);
    }

    private static void handleEmptyType(SolrInputDocument solrDoc, String fieldName) {
        // No actual data to add, but we can acknowledge its existence.
        solrDoc.addField(fieldName, "__EMPTY__");
    }

    private static void handleFloatType(SolrInputDocument solrDoc, FloatValue floatValue, String fieldName) {
        // Convert protobuf FloatValue to a Java float
        float javaFloat = floatValue.getValue();
        solrDoc.addField(fieldName, javaFloat);
    }

    private static void handleBytesType(SolrInputDocument solrDoc, BytesValue bytesValue, String fieldName) {
        // Convert protobuf BytesValue to String
        String byteString = bytesValue.getValue().toStringUtf8();
        solrDoc.addField(fieldName, byteString);
    }

    private static void handleDurationType(SolrInputDocument solrDoc, Duration duration, String fieldName) {
        // Convert protobuf Duration to java.time.Duration
        java.time.Duration javaDuration = java.time.Duration.ofSeconds(duration.getSeconds(), duration.getNanos());
        solrDoc.addField(fieldName, javaDuration.toString());
    }

    private static void handleTimestampType(SolrInputDocument solrDoc, Timestamp timestamp, String fieldName) {
        // Handle Timestamp fields
        // Convert to java.util.Date then add to solrDoc
        long milliseconds = timestamp.getSeconds() * 1000L + timestamp.getNanos() / 1000000;
        Date javaDate = new Date(milliseconds);
        solrDoc.addField(fieldName, javaDate);
    }

    private void extractFieldsFromStruct(Struct struct, SolrInputDocument solrDoc, String prefix) {
        Map<String, Value> fields = struct.getFieldsMap();

        for (Map.Entry<String, Value> entry : fields.entrySet()) {
            String newFieldKey = prefix + "_" + entry.getKey();
            Value.KindCase type = entry.getValue().getKindCase();

            switch(type){
                case BOOL_VALUE:
                    solrDoc.addField(newFieldKey, entry.getValue().getBoolValue());
                    break;
                case NUMBER_VALUE:
                    solrDoc.addField(newFieldKey, entry.getValue().getNumberValue());
                    break;
                case STRING_VALUE:
                    solrDoc.addField(newFieldKey, entry.getValue().getStringValue());
                    break;
                case LIST_VALUE:
                    ListValue listValue = entry.getValue().getListValue();
                    for (Value listItem : listValue.getValuesList()) {
                        solrDoc.addField(newFieldKey, listItem.toString());
                    }
                    break;
                case STRUCT_VALUE:
                    extractFieldsFromStruct(entry.getValue().getStructValue(), solrDoc, newFieldKey);
                    break;
                case NULL_VALUE:
                    solrDoc.addField(newFieldKey, null);
                    break;
            }
        }
    }
}