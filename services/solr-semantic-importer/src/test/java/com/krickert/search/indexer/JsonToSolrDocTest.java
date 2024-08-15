package com.krickert.search.indexer;

import com.krickert.search.indexer.solr.HttpSolrSelectResponse;
import com.krickert.search.indexer.solr.JsonToSolrDoc;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JsonToSolrDoc class.
 * Specifically, it tests the parseSolrDocuments method.
 */
public class JsonToSolrDocTest {


    /**
     * Test checks if the parseSolrDocuments method is able to parse a JSON string into a collection of SolrDocument objects.
     */
    @Test
    public void testParseSolrDocuments() {
        // Arrange
        JsonToSolrDoc jsonToSolrDoc = new JsonToSolrDoc();
        String jsonString =  "{" +
                "  \"responseHeader\":{\n" +
                "    \"status\":0,\n" +
                "    \"QTime\":1,\n" +
                "    \"params\":{\n" +
                "      \"q\":\"example\",\n" +
                "      \"indent\":\"true\",\n" +
                "      \"wt\":\"json\"}}," +
                "\"response\": {" +
                "\"numFound\":2,\"start\":0," +
                "\"docs\": [" +
                "{" +
                "\"id\": \"doc1\"," +
                "\"field1\": \"value1\"," +
                "\"field2\": 123," + // integer
                "\"field3\": 123.45," + // float
                "\"field4\": [1, 2, 3]," + // array of integers
                "\"field5\": [\"str1\", \"str2\", \"str3\"]," + // array of strings
                "\"field6\": [123.4, 456.3, 789.6]," + // array of integers
                "\"_version_\": \"8675309\"" +
                "}," +
                "{" +
                "\"id\": \"doc2\"," +
                "\"field1\": \"value3\"," +
                "\"field2\": 456," + // integer
                "\"field3\": 456.78," + // float
                "\"field4\": [4, 5, 6]," + // array of integers
                "\"field5\": [\"str4\", \"str5\", \"str6\"]," + // array of strings
                "\"field6\": [987.2, 654.3, 321.4]," + // array of integers
                "\"_version_\": \"867530999\"" +
                "}" +
                "]" +
                "}" +
                "}";

        // Act
        HttpSolrSelectResponse httpSolrSelectResponse = jsonToSolrDoc.parseSolrDocuments(jsonString);
        Collection<SolrInputDocument> solrDocuments = httpSolrSelectResponse.getDocs();
        // Assert
        assertEquals(2, solrDocuments.size());
        solrDocuments.forEach(doc -> {
            assertTrue(doc.containsKey("id"));
            assertTrue(doc.containsKey("field1"));
            assertTrue(doc.containsKey("field2"));
            assertTrue(doc.containsKey("field3"));
            assertTrue(doc.containsKey("field4"));
            assertTrue(doc.containsKey("field5"));
            assertTrue(doc.containsKey("field6"));
            assertFalse(doc.containsKey("_version_"));
        });
    }
    /**
     * Test checks if the parseSolrDocuments method throws an exception when it tries to parse an invalid JSON string.
     */
    @Test
    public void testParseSolrDocumentsWithInvalidJson() {
        // Arrange
        JsonToSolrDoc jsonToSolrDoc = new JsonToSolrDoc();
        String invalidJsonString = "This is not a valid JSON string.";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jsonToSolrDoc.parseSolrDocuments(invalidJsonString));
    }
}