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
            String fieldName = prefix.isEmpty() ? entry.getKey().getName() : prefix + "_" + entry.getKey().getName();
            if (entry.getValue() instanceof Message) {
                if (entry.getValue() instanceof Struct) {
                    extractFieldsFromStruct((Struct) entry.getValue(), solrDoc, fieldName);
                } else if (entry.getValue() instanceof Timestamp timestamp) {
                    // Handle Timestamp fields
                    // Convert to java.util.Date then add to solrDoc
                    long milliseconds = timestamp.getSeconds() * 1000L + timestamp.getNanos() / 1000000;
                    java.util.Date javaDate = new java.util.Date(milliseconds);
                    solrDoc.addField(fieldName, javaDate);
                } else {
                    addFieldsToSolrDoc((Message) entry.getValue(), solrDoc, fieldName);
                }
            } else if (entry.getKey().isMapField()) {
                @SuppressWarnings("unchecked") Map<Object, Object> mapValue = (Map<Object, Object>) entry.getValue();
                for (Map.Entry<Object, Object> mapEntry : mapValue.entrySet()) {
                    solrDoc.addField(fieldName + "_" + mapEntry.getKey(), mapEntry.getValue());
                }
            } else if (entry.getKey().isRepeated()) {
                @SuppressWarnings("unchecked") List<Object> listValue = (List<Object>) entry.getValue();
                for (Object item : listValue) {
                    solrDoc.addField(fieldName, item);
                }
            } else {
                solrDoc.addField(fieldName, entry.getValue());
            }
        }
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