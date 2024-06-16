package com.krickert.search.indexer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Singleton
public class JsonToSolrDoc {
    ObjectMapper mapper = new ObjectMapper();

    public Collection<SolrDocument> parseSolrDocuments(String jsonString) {
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> response = (Map<String, Object>)map.get("response");
        List<Map<String, Object>> docs = (List<Map<String, Object>>)response.get("docs");
        List<SolrDocument> solrDocuments = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            SolrDocument solrDoc = new SolrDocument();
            doc.forEach((k, v) -> {
                SolrInputField inputField = new SolrInputField(k);
                inputField.setValue(v);
                solrDoc.put(k, inputField);
            });
            solrDoc.remove("_version_");
            solrDocuments.add(solrDoc);
        }
        return solrDocuments;
    }
}