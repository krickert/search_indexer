package com.krickert.search.installer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SolrInstallerOptions {
    private final String workSpaceLocation;
    private final String solrVersion;
    private final String solrArchive;
    private final String solrInstallDir;
    private final String solrModulesDir;
    private final String solrOpenNlpDir;
    private final String solrExec;
    private final String solrCollectionName;
    private final String solrDownloadUrl;
    private final String solrUserName;
    private final String solrPassword;
    private final String solrNlpConfigDir;
    private final String solrNlpModelsDir;

    public SolrInstallerOptions(
            @Value("${workspace.location}")String workSpaceLocation,
            @Value("${solr.version}")String solrVersion,
            @Value("${solr.archive}")String solrArchive,
            @Value("${solr.install.dir}")String solrInstallDir,
            @Value("${solr.modules.dir}")String solrModulesDir,
            @Value("${solr.opennlp.dir}")String solrOpenNlpDir,
            @Value("${solr.exec}")String solrExec,
            @Value("${solr.collection.name}")String solrCollectionName,
            @Value("${solr.url}")String solrDownloadUrl,
            @Value("${solr.auth.username}")String solrUserName,
            @Value("${solr.auth.password}")String solrPassword,
            @Value("${solr.nlp.config.dir}")String solrNlpConfigDir,
            @Value("${solr.opennlp.models.dir}")String solrNlpModelsDir) {
        this.workSpaceLocation = workSpaceLocation;
        this.solrVersion = solrVersion;
        this.solrArchive = solrArchive;
        this.solrInstallDir = solrInstallDir;
        this.solrModulesDir = solrModulesDir;
        this.solrOpenNlpDir = solrOpenNlpDir;
        this.solrExec = solrExec;
        this.solrCollectionName = solrCollectionName;
        this.solrDownloadUrl = solrDownloadUrl;
        this.solrUserName = solrUserName;
        this.solrPassword = solrPassword;
        this.solrNlpConfigDir = solrNlpConfigDir;
        this.solrNlpModelsDir = solrNlpModelsDir;

    }

    public String getWorkSpaceLocation() {
        return workSpaceLocation;
    }

    public String getSolrVersion() {
        return solrVersion;
    }

    public String getSolrArchive() {
        return solrArchive;
    }

    public String getSolrInstallDir() {
        return solrInstallDir;
    }

    public String getSolrModulesDir() {
        return solrModulesDir;
    }

    public String getSolrExec() {
        return solrExec;
    }

    public String getSolrOpenNlpDir() {
        return solrOpenNlpDir;
    }

    public String getSolrCollectionName() {
        return solrCollectionName;
    }

    public String getSolrDownloadUrl() {
        return solrDownloadUrl;
    }

    public String getSolrUserName() {
        return solrUserName;
    }

    public String getSolrPassword() {
        return solrPassword;
    }

    public String getSolrNlpConfigDir() {
        return solrNlpConfigDir;
    }

    public String getSolrNlpModelsDir() {
        return solrNlpModelsDir;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("workSpaceLocation", workSpaceLocation)
                .append("solrVersion", solrVersion)
                .append("solrArchive", solrArchive)
                .append("solrInstallDir", solrInstallDir)
                .append("solrModulesDir", solrModulesDir)
                .append("solrOpenNlpDir", solrOpenNlpDir)
                .append("solrExec", solrExec)
                .append("solrCollectionName", solrCollectionName)
                .append("solrDownloadUrl", solrDownloadUrl)
                .append("solrUserName", solrUserName)
                .append("solrPassword", solrPassword)
                .append("solrNlpConfigDir", solrNlpConfigDir)
                .append("solrNlpModelsDir", solrNlpModelsDir)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof SolrInstallerOptions that)) return false;

        return new EqualsBuilder().append(workSpaceLocation, that.workSpaceLocation).append(solrVersion, that.solrVersion).append(solrArchive, that.solrArchive).append(solrInstallDir, that.solrInstallDir).append(solrModulesDir, that.solrModulesDir).append(solrOpenNlpDir, that.solrOpenNlpDir).append(solrExec, that.solrExec).append(solrCollectionName, that.solrCollectionName).append(solrDownloadUrl, that.solrDownloadUrl).append(solrUserName, that.solrUserName).append(solrPassword, that.solrPassword).append(solrNlpConfigDir, that.solrNlpConfigDir).append(solrNlpModelsDir, that.solrNlpModelsDir).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(workSpaceLocation).append(solrVersion).append(solrArchive).append(solrInstallDir).append(solrModulesDir).append(solrOpenNlpDir).append(solrExec).append(solrCollectionName).append(solrDownloadUrl).append(solrUserName).append(solrPassword).append(solrNlpConfigDir).append(solrNlpModelsDir).toHashCode();
    }
}
