package com.krickert.search.installer;

import com.krickert.util.download.Downloader;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.schema.SchemaRequest;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class SolrInstaller {
    private final Logger logger = LoggerFactory.getLogger(SolrInstaller.class);
    private final String workspaceLocation;
    private final String solrArchive;
    private final String solrCollectionName;
    private final String solrDownloadUrl;
    private final Boolean isDownloadSolr;
    private final Boolean isInstallSolr;
    private final Downloader downloader;

    @Autowired
    public SolrInstaller(
            @Value("${workspace.location}") String workspaceLocation,
            @Value("${solr.archive}") String solrArchive,
            @Value("${solr.collection.name}") String solrCollectionName,
            @Value("${solr.url}") String solrDownloadUrl,
            @Value("${download.solr}") Boolean isDownloadSolr,
            @Value("${install.solr}") Boolean isInstallSolr,
            Downloader downloader) {
        this.workspaceLocation = workspaceLocation;
        this.solrArchive = solrArchive;
        this.downloader = downloader;
        this.solrCollectionName = solrCollectionName;
        this.solrDownloadUrl = solrDownloadUrl;
        this.isDownloadSolr = isDownloadSolr;
        this.isInstallSolr = isInstallSolr;
    }


    public void installSolr() throws IOException, InterruptedException {
        File destination = new File(workspaceLocation);

        String solrTgzFilename = workspaceLocation + solrArchive;
        File solr_tgz = new File(solrTgzFilename);
        if (isDownloadSolr) {
            solr_tgz = downloadSolr(solrTgzFilename);
        }
        if (isInstallSolr) {
            installSolr(destination, solr_tgz);
            createCollection(solrCollectionName);
        }
    }

    private File downloadSolr(String solrTgzFilename) {
        //download solr
        logger.info("Downloading Solr 9");
        try {
            return downloader.download(new URL(solrDownloadUrl), new File(solrTgzFilename));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void installSolr(File destination, File solr_tgz) throws IOException, InterruptedException {
        //unzip solr
        logger.info("extracting solr 9 to " + workspaceLocation);
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(solr_tgz, destination);
        //this will simply start up solr in standalone zookeeper cloud mode
        String solrExec = workspaceLocation + "/solr-9.0.0/bin/solr";
        String[] cmd = {solrExec, "-c", "-m", "3g"};
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }


    private void createCollection(String collection) {
        boolean collectionCreated = false;
        int tries = 0;
        while (tries < 5 && !collectionCreated) {
            try (SolrClient client = new Http2SolrClient.Builder("http://localhost:8983/solr").build()) {
                CollectionAdminRequest.Create creator = CollectionAdminRequest.createCollection(collection, 1, 1);
                client.request(creator);
                SolrPingResponse response = client.ping(collection);
                if (response.getStatus() == 0) {
                    collectionCreated = true;
                } else {
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                logger.error("collection was not created or ping failed", e);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
            tries++;
        }

        //now create the schema fields
        SchemaRequest schemaRequest = new SchemaRequest();

    }

}
