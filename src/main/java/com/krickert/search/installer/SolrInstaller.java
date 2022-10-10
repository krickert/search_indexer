package com.krickert.search.installer;

import com.krickert.util.download.Downloader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Component
public class SolrInstaller {
    private final Logger logger = LoggerFactory.getLogger(SolrInstaller.class);
    private final Boolean isDownloadSolr;
    private final Boolean isInstallSolr;
    private final Downloader downloader;
    private final OpenNlpInstaller openNlpInstaller;
    private final SolrInstallerOptions opts;

    @Autowired
    public SolrInstaller(
            @Value("${workspace.location}") String workspaceLocation,
            @Value("${solr.archive}") String solrArchive,
            @Value("${solr.collection.name}") String solrCollectionName,
            @Value("${solr.url}") String solrDownloadUrl,
            @Value("${download.solr}") Boolean isDownloadSolr,
            @Value("${install.solr}") Boolean isInstallSolr,
            Downloader downloader,
            OpenNlpInstaller openNlpInstaller,
            SolrInstallerOptions opts) {
        this.opts = opts;
        this.downloader = downloader;
        this.isDownloadSolr = isDownloadSolr;
        this.isInstallSolr = isInstallSolr;
        this.openNlpInstaller = openNlpInstaller;
    }


    public void installSolr() throws IOException, InterruptedException {
        File destination = new File(opts.getWorkSpaceLocation());

        String solrTgzFilename = opts.getWorkSpaceLocation() + "/" + opts.getSolrArchive();
        File solr_tgz = new File(solrTgzFilename);
        if (isDownloadSolr) {
            solr_tgz = downloadSolr(solrTgzFilename);
        }
        if (isInstallSolr) {
            installSolr(destination, solr_tgz);
            openNlpInstaller.downloadOpennlpModels();
            extractSolrConfig();
            setupSolrSecurity();
            uploadSolrConfig();
            createCollection();
        }
    }

    private void uploadSolrConfig() throws IOException, InterruptedException {
        String[] cmd = {"zk", "upconfig",  "-n", opts.getSolrCollectionName(), "-d", opts.getSolrNlpConfigDir(), "-z", "localhost:9983"};
        runSolrCmd(cmd);
    }

    private void setupSolrSecurity() throws InterruptedException, IOException {
        logger.info("copying security file to config");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource securityJsonResource = resolver.getResource("solr/security.json");
        FileUtils.copyURLToFile(securityJsonResource.getURL(), new File(opts.getWorkSpaceLocation() + "/security.json"));
        Resource solrInShResource = resolver.getResource("solr/solr.in.sh");
        FileUtils.copyURLToFile(solrInShResource.getURL(), new File(opts.getSolrInstallDir() + "/bin/solr.in.sh"));
        //Upload security.json to zookeeper
        String[] uploadSecurityJson =
                {"zk", "cp", opts.getWorkSpaceLocation() + "/security.json", "zk:/security.json",  "-z",  "localhost:9983"};
        runSolrCmd(uploadSecurityJson);
        restartSolr();
        //security is now enabled
    }

    private void restartSolr() throws IOException, InterruptedException {
        logger.info("restarting solr");
        stopSolr();
        startSolr();
    }

    private void stopSolr() throws IOException, InterruptedException {
        logger.info("stopping solr");
        String[] solrStopCmd = {"stop"};
        runSolrCmd(solrStopCmd);
    }

    private void extractSolrConfig() throws IOException {
        String configDir = opts.getSolrNlpConfigDir();
        PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
        logger.info("Staging the solr config set that contains the collection setup.  This has the schema fields as well as opennlp config that belongs in zookeeper.");
        Resource[] resources = r.getResources("/solr/configsets/wikipedia_demo/**");
        for (Resource resource : resources) {
            URL inputURL = resource.getURL();
            String fileToSave = StringUtils.substringAfterLast(inputURL.getPath(), "wikipedia_demo");
            File file = new File(configDir + "/" + fileToSave);
            file.getParentFile().mkdirs();
            if (StringUtils.equalsAny(fileToSave,"/conf/","/conf/lang/")) {
                file.mkdirs();
            } else {
                FileUtils.copyURLToFile(inputURL, file);
                logger.info("Copied " + inputURL + " to " + file.getAbsolutePath());
            }
        }
    }

    private File downloadSolr(String solrTgzFilename) {
        //download solr
        logger.info("Downloading Solr");
        try {
            return downloader.download(new URL(opts.getSolrDownloadUrl()), new File(solrTgzFilename));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private void installSolr(File destination, File solr_tgz) throws IOException, InterruptedException {
        //unzip solr
        logger.info("extracting solr 9 to " + opts.getWorkSpaceLocation());
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(solr_tgz, destination);
        startSolr();
    }

    private void runSolrCmd(String[] solrArgs) throws IOException, InterruptedException {
        //this will simply start up solr in standalone zookeeper cloud mode
        String[] cmd = ArrayUtils.addFirst(solrArgs, opts.getSolrExec());
        logger.info("Executing solr command: {}", ArrayUtils.toString(cmd));
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }

    private void startSolr() throws IOException, InterruptedException {
        String[] cmd = {"-c", "-m", "3g"};
        runSolrCmd(cmd);
    }


    private void createCollection() {
        boolean collectionCreated = false;
        int tries = 0;
        while (tries < 5 && !collectionCreated) {
            //TODO: is this retry logic overkill?  if the code gets here, solr has to be running.
            //TODO: keep for now, but consider removing because it feels sorta pointless since we're waiting for the start now
            try (SolrClient client =
                         new Http2SolrClient
                                 .Builder("http://localhost:8983/solr")
                                    .withBasicAuthCredentials(opts.getSolrUserName(), opts.getSolrPassword())
                                    .build())
            {
                CollectionAdminRequest.Create creator = CollectionAdminRequest.createCollection(opts.getSolrCollectionName(), opts.getSolrCollectionName(), 1, 1);
                client.request(creator);
                SolrPingResponse response = client.ping(opts.getSolrCollectionName());
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

    }

}
