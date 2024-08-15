package com.krickert.search.indexer.solr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public class JsonToSolrDoc {
    ObjectMapper mapper = new ObjectMapper();

    public Collection<String> parseSolrDocumentsToJSON(String jsonString) {
        Map<String, Object> map;
        try {
            map = mapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> response = (Map<String, Object>)map.get("response");
        List<Map<String, Object>> docs = (List<Map<String, Object>>)response.get("docs");

        List<String> jsonDocuments = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            try {
                doc.remove("_version_");
                jsonDocuments.add(mapper.writeValueAsString(doc));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return jsonDocuments;
    }

    public HttpSolrSelectResponse parseSolrDocuments(String jsonString) {
        Map<String, Object> map;
        try {
            map = mapper.readValue(jsonString, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> response = (Map<String, Object>)map.get("response");
        Map<String, Object> responseHeader = (Map<String, Object>)map.get("responseHeader");
        List<Map<String, Object>> docs = (List<Map<String, Object>>)response.get("docs");

        List<SolrInputDocument> solrDocuments = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            SolrInputDocument solrDoc = new SolrInputDocument();
            doc.forEach((k, v) -> {
                SolrInputField inputField = new SolrInputField(k);
                inputField.setValue(v);
                solrDoc.put(k, inputField);
            });
            solrDoc.remove("_version_");
            solrDocuments.add(solrDoc);
        }

        Long numFound = Long.parseLong(response.get("numFound").toString());
        Long qtime = Long.parseLong(responseHeader.get("QTime").toString());
        Long start = Long.parseLong(response.get("start").toString());

        // Assuming pageSize is not available in the json response
        Long pageSize = null;

        // Build the response object

        return new HttpSolrSelectResponse.Builder()
                .numFound(numFound)
                .qtime(qtime)
                .start(start)
                .docs(solrDocuments)
                .pageSize(pageSize)
                .build();
    }
}