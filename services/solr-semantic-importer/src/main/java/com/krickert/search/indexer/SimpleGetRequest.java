package com.krickert.search.indexer;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.netty.DefaultHttpClient;
import jakarta.inject.Singleton;

import java.net.URI;
import java.net.URISyntaxException;

@Singleton
public class SimpleGetRequest {

    public String getResponseAsString(String url)  {
        final URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpClient client = new DefaultHttpClient(uri);

        String response = client.toBlocking().retrieve(HttpRequest.GET(uri));
        client.close();
        return response;
    }

}