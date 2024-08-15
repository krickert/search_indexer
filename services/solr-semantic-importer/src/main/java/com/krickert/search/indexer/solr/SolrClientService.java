package com.krickert.search.indexer.solr;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.RequestWriter;
import org.apache.solr.client.solrj.SolrRequest;
import org.eclipse.jetty.http.HttpHeader;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Singleton
public class SolrClientService {

    @Value("${indexer.destination.solr-connection.authentication.token-url}")
    private String tokenUrl;

    @Value("${indexer.destination.solr-connection.authentication.client-id}")
    private String clientId;

    @Value("${indexer.destination.solr-connection.authentication.client-secret}")
    private String clientSecret;

    @Inject
    @Client("${indexer.destination.solr-connection.authentication.token-url}")
    HttpClient oktaHttpClient;

    public Http2SolrClient createSolrClient(String solrUrl) throws InterruptedException, ExecutionException, TimeoutException {
        String accessToken = getOktaAccessToken();

        return new Http2SolrClient.Builder(solrUrl)
                .withRequestWriter(new CustomRequestWriter(accessToken))
                .build();
    }

    private String getOktaAccessToken() throws InterruptedException, ExecutionException, TimeoutException {
        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpResponse<Map> response = oktaHttpClient.toBlocking()
                .exchange(io.micronaut.http.HttpRequest.POST(tokenUrl, body)
                          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                          .accept(MediaType.APPLICATION_JSON), Map.class);

        if (response.getStatus().getCode() == 200) {
            Map<String, Object> responseBody = response.body();
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Failed to retrieve access token: " + response.getStatus());
        }
    }

    public static void main(String[] args) {
        Micronaut.run(SolrClientService.class, args);
    }

    // CustomRequestWriter to add authorization header
    public static class CustomRequestWriter extends RequestWriter {
        private final String accessToken;

        public CustomRequestWriter(String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public void write(SolrRequest<?> request, OutputStream os) throws IOException {
            request.setBasePath(request.getBasePath());
            request.getHeaders().put(HttpHeader.AUTHORIZATION.asString(), "Bearer " + accessToken);
            super.write(request, os);
        }
    }
}