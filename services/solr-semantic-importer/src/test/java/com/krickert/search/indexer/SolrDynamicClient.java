package com.krickert.search.indexer;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;

@Singleton
public class SolrDynamicClient {
    private final HttpClient httpClient;

    public SolrDynamicClient(@Client HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpResponse<String> sendJsonToSolr(String solrURL, String collection, String jsonDocument) throws HttpClientResponseException {
        return httpClient.toBlocking().exchange(HttpRequest.POST(solrURL + "/" + collection + "/update", jsonDocument).contentType("application/json"), String.class);
    }

    public HttpResponse<String> createCollection(String solrURL, String collectionName) throws HttpClientResponseException {
        String collectionAPI = "/admin/collections?action=CREATE&name=" + collectionName + "&numShards=1&replicationFactor=1&maxShardsPerNode=1&collection.configName=_default";
        return httpClient.toBlocking().exchange(HttpRequest.GET(solrURL + collectionAPI), String.class);
    }

    public HttpResponse<String> deleteCollection(String solrURL, String collectionName) throws HttpClientResponseException {
        String collectionAPI = "/admin/collections?action=DELETE&name=" + collectionName;
        return httpClient.toBlocking().exchange(HttpRequest.GET(solrURL + collectionAPI), String.class);
    }
}