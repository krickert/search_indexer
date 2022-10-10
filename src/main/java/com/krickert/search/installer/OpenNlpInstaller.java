package com.krickert.search.installer;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.krickert.util.download.Downloader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
@Component
public class OpenNlpInstaller {
    private final Logger logger = LoggerFactory.getLogger(OpenNlpInstaller.class);

    private final String workspaceLocation;
    private final Downloader downloader;
    private final List<String> openNlpModelUrlDownloads;
    private final String openNlpDownloadUrl;
    private final String openNlpDownloadUrlSha512;
    private final String openNlpFullPath;
    private final String openNlpModelPrefixUrl;
    private final SolrInstallerOptions opts;

    @Autowired
    public OpenNlpInstaller(
            Downloader downloader,
            @Value("${workspace.location}") String workspaceLocation,
            @Value("${opennlp.model.download.urls}") List<String> openNlpModelUrlDownloads,
            @Value("${opennlp.download.url}") String openNlpDownloadUrl,
            @Value("${opennlp.download.url.sha512}") String openNlpDownloadUrlSha512,
            @Value("${opennlp.fullpath}") String openNlpFullPath,
            @Value("${opennlp.install.location}") String openNlpInstallLocation,
            @Value("${opennlp.model.prefix}") String openNlpModelPrefixUrl, SolrInstallerOptions opts) {
        this.downloader = downloader;
        this.opts = opts;
        this.workspaceLocation = workspaceLocation;
        this.openNlpModelUrlDownloads = openNlpModelUrlDownloads;
        this.openNlpDownloadUrl = openNlpDownloadUrl;
        this.openNlpDownloadUrlSha512 = openNlpDownloadUrlSha512;
        this.openNlpFullPath = openNlpFullPath;
        this.openNlpModelPrefixUrl = openNlpModelPrefixUrl;
    }

    public void downloadOpennlpModels() {
        logger.info("Downloading opennlp archive");
        try {
            String savePath = opts.getSolrNlpModelsDir();
            File savePathDirectory = new File(savePath);
            boolean result = savePathDirectory.mkdirs();
            if (!result) {
                logger.warn("directory {} was not created.", savePath);
            }
            logger.info("downloading the model files to [{}]", savePath);
            for (String modelUrl : openNlpModelUrlDownloads) {
                String fileName = extractFilenameFromModelUrl(modelUrl);
                String fullPath = savePath + "/" + fileName;
                downloader.download(new URL(modelUrl), new File(fullPath));
                //TODO: we're not validating the download but that's NBD for now
            }
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            Resource enLemm = r.getResource("solr/module/opennlp/models/en-lemmatizer.bin");
            FileUtils.copyURLToFile(enLemm.getURL(), new File(savePath + "/en-lemmatizer.bin"));
            Resource stopPosTxt = r.getResource("solr/module/opennlp/models/stop.pos.txt");
            FileUtils.copyURLToFile(stopPosTxt.getURL(), new File(savePath + "/stop.pos.txt"));
            createModelJar();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createModelJar() throws IOException, InterruptedException {
        //jar --create --file models.jar -C /Users/kristianrickert/search_scratch/ models
        String[] cmd = {"jar", "--create", "--file", opts.getSolrOpenNlpDir() + "/models.jar", "-C", opts.getWorkSpaceLocation(),  "models"};
        //this will simply start up solr in standalone zookeeper cloud mode
        logger.info("Executing jar command: {}", ArrayUtils.toString(cmd));
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
    }

    private String extractFilenameFromModelUrl(String modelUrl) {
        return StringUtils.remove(modelUrl, openNlpModelPrefixUrl);
    }

    private void untarOpenNlp(File openNlpArchive) throws IOException {
        Archiver archiver = ArchiverFactory.createArchiver("tar", "gz");
        archiver.extract(openNlpArchive, new File(workspaceLocation));
    }

    private boolean validateSha512(File downloadedFile, File downloadedSha512) throws IOException {
        String sha512Measured = Files.asByteSource(downloadedFile).hash(Hashing.sha512()).toString();
        String expectedSha512 = Files.asCharSource(downloadedSha512, Charsets.UTF_8).read();
        return StringUtils.startsWith(expectedSha512, sha512Measured);
    }

    private File downloadOpenNlpArchive() throws IOException {
        File openNlpArchive = downloader.download(new URL(openNlpDownloadUrl), new File(openNlpFullPath));
        String openNlpSha512FileName = openNlpFullPath + ".sha512";
        File openNlpSha512File = downloader.download(new URL(openNlpDownloadUrlSha512), new File(openNlpSha512FileName));
        boolean does512ShaMatch = validateSha512(openNlpArchive, openNlpSha512File);
        if(does512ShaMatch) {
            logger.info("Successfully downloaded open nlp.  Extracting the archive.");
        } else {
            logger.error("SHA512 check for opennlp files failed.  Please check the files [{}] and [{}]", openNlpFullPath, openNlpSha512FileName);
            throw new IllegalStateException("Open NLP download failed due to mismatched sha512.  Please check the logs. ");
        }
        return openNlpArchive;
    }


}
