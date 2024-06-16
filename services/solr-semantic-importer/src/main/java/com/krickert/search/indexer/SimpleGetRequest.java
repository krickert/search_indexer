package com.krickert.search.indexer;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.netty.DefaultHttpClient;

import java.net.URI;
import java.net.URISyntaxException;

public class SimpleGetRequest {
    public String getResponseAsString(String url)  {
        final URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpClient client = new DefaultHttpClient(uri);

        String response = client.toBlocking().retrieve(HttpRequest.GET("/"));
        client.close();
        return response;
    }

    public static void main(String[] args) {
        SimpleGetRequest check = new SimpleGetRequest();
        String url = "http://example.com";
        String response = check.getResponseAsString(url);
        System.out.println(response);
    }
}