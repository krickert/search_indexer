package com.krickert.search.indexer.solr.component;

import com.google.common.collect.Lists;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.ConfigSetAdminRequest;
import org.apache.solr.client.solrj.response.ConfigSetAdminResponse;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Singleton
public class SolrHelper {

    private static final Logger log = LoggerFactory.getLogger(SolrHelper.class);

    private final String solrHost;

     private final String solrPort;


    private final SolrClient client;

    @Inject
    public SolrHelper(@Value("${solr.port}")String solrPort, @Value("${solr.host}") String solrHost) {
        this.solrHost = solrHost;
        this.solrPort = solrPort;
        this.client = new Http2SolrClient.Builder("http://" + solrHost + ":" + solrPort + "/solr").withBasicAuthCredentials("solr", "SolrRocks").build();
    }

    public void uploadConfig() throws SolrServerException, IOException, InterruptedException, KeeperException {
        uploadConfigSet(client);
    }

    public void reset() {
        reset(client);
    }

    public static void enableSecurityPlugin() throws IOException, InterruptedException, KeeperException {
        String securityJson = "{\n" +
                "   \"authentication\":{\n" +
                "       \"blockUnknown\": true,\n" +
                "       \"class\":\"solr.BasicAuthPlugin\",\n" +
                "       \"credentials\":{\"solr\":\"IV0EHq1OnNrj6gvRCwvFwTrZ1+z1oBbnQdiVC3otuq0= Ndd7LKvVBAaZIF0QAVi1ekCfAJXr1GGfLtRUXhgrF8c=\", \"guest\":\"BcGRASTT1MhFRC7B4Ii/PSWt68e+WuNmYnthLczEauw= Ndd7LKvVBAaZIF0QAVi1ekCfAJXr1GGfLtRUXhgrF8c=\"}},\n" +
                "   \"authorization\":{\n" +
                "       \"class\":\"solr.RuleBasedAuthorizationPlugin\",\n" +
                "       \"permissions\":[" +
                "           {\"name\":\"security-edit\", \"role\":\"admin\"}," +
                "           {\"name\":\"security-read\", \"role\":\"admin\"}," +
                "           {\"name\":\"collection-admin-read\", \"role\":\"admin\"}," +
                "           {\"name\":\"collection-admin-edit\", \"role\":\"admin\"}," +
                "           {\"name\":\"all\", \"role\":\"admin\"}" +
                "       ],\n" +
                "       \"user-role\":{\"solr\":\"admin\", \"guest\":\"guest\"}\n" +
                "   }\n" +
                "}";

        String zkHost = "localhost:2181";  // Your ZooKeeper connection string

        ZooKeeper zooKeeper = new ZooKeeper(zkHost, 3000, w -> {});

        // Ensure that ZooKeeper has '/security.json' zNode
        if (zooKeeper.exists("/security.json", false) == null) {
            zooKeeper.create("/security.json", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }

        // Updating the zNode data
        zooKeeper.setData("/security.json", securityJson.getBytes(), -1);

        System.out.println("Authentication and Authorization should now be enabled on the Solr instance!");
    }

    private static void uploadConfigSet(SolrClient client) throws SolrServerException, IOException, InterruptedException, KeeperException {
        enableSecurityPlugin();
        ConfigSetAdminRequest.Upload request = new ConfigSetAdminRequest.Upload();
        request.setConfigSetName("wiki_config");

        ResourceResolver resolver = new ResourceResolver();
        File resource = new File(resolver.getResource("classpath:wiki-config.zip").get().getFile());

        request.setUploadFile(resource, "zip" );


        // Execute the request
        ConfigSetAdminResponse response = request.process(client);


        // Check the response status
        if (response.getStatus() == 0) {
            System.out.println("Configset uploaded successfully!");
        } else {
            System.out.println("Error uploading configset: " + response);
        }
        client.request(CollectionAdminRequest.createCollection("wiki_article", "wiki_config",3, 1));
        client.request(CollectionAdminRequest.createCollection("wiki_paragraph", "wiki_config",3, 1));

    }
    private static void reset(SolrClient client)  {
        try {
            client.request(CollectionAdminRequest.deleteCollection("wiki_article"));
            log.info("wiki_article deleted successfully!");
        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException e) {
            log.error("wiki_article delete error");
        }
        try {
            client.request(CollectionAdminRequest.deleteCollection("wiki_paragraph"));
            log.info("wiki_paragraph deleted successfully!");
        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException e) {
            log.error("wiki_paragraph delete error");
        }

        ConfigSetAdminRequest.Delete request = new ConfigSetAdminRequest.Delete();
        request.setConfigSetName("wiki_config");
        try {
            // Execute the request
            ConfigSetAdminResponse response = request.process(client);
            // Check the response status
            if (response.getStatus() == 0) {
                log.info("Configset deleted successfully!");
            } else {
                log.info("Error deleting configset: {}", response);
            }
        } catch (IOException | SolrServerException | BaseHttpSolrClient.RemoteSolrException  e) {
            log.error("config delete error");
        }


    }

}
