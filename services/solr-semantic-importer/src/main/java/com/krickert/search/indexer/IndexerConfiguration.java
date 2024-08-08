package com.krickert.search.indexer;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
@ConfigurationProperties("indexer")
@Introspected
public class IndexerConfiguration {

    private Source source;
    private Destination destination;

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    @ConfigurationProperties("source")
    @Introspected
    public static class Source {

        private String solrVersion;
        private String solrCollection;
        private SeedData seedData;
        private SolrConnection solrConnection;

        public String getSolrVersion() {
            return solrVersion;
        }

        public void setSolrVersion(String solrVersion) {
            this.solrVersion = solrVersion;
        }

        public String getSolrCollection() {
            return solrCollection;
        }

        public void setSolrCollection(String solrCollection) {
            this.solrCollection = solrCollection;
        }

        public SeedData getSeedData() {
            return seedData;
        }

        public void setSeedData(SeedData seedData) {
            this.seedData = seedData;
        }

        public SolrConnection getSolrConnection() {
            return solrConnection;
        }

        public void setSolrConnection(SolrConnection solrConnection) {
            this.solrConnection = solrConnection;
        }

        @ConfigurationProperties("seed-data")
        @Introspected
        public static class SeedData {

            private boolean enabled;
            private String seedJsonFile;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getSeedJsonFile() {
                return seedJsonFile;
            }

            public void setSeedJsonFile(String seedJsonFile) {
                this.seedJsonFile = seedJsonFile;
            }
        }

        @ConfigurationProperties("solr-connection")
        @Introspected
        public static class SolrConnection {

            private String url;
            private Authentication authentication;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public Authentication getAuthentication() {
                return authentication;
            }

            public void setAuthentication(Authentication authentication) {
                this.authentication = authentication;
            }

            @ConfigurationProperties("authentication")
            @Introspected
            public static class Authentication {

                private boolean enabled;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }
            }
        }
    }

    @ConfigurationProperties("destination")
    @Introspected
    public static class Destination {

        private String solrVersion;
        private String solrCollection;
        private SolrConnection solrConnection;
        private Map<String, VectorConfig> vectorConfig;

        public String getSolrVersion() {
            return solrVersion;
        }

        public void setSolrVersion(String solrVersion) {
            this.solrVersion = solrVersion;
        }

        public String getSolrCollection() {
            return solrCollection;
        }

        public void setSolrCollection(String solrCollection) {
            this.solrCollection = solrCollection;
        }

        public SolrConnection getSolrConnection() {
            return solrConnection;
        }

        public void setSolrConnection(SolrConnection solrConnection) {
            this.solrConnection = solrConnection;
        }

        @ConfigurationProperties("solr-connection")
        @Introspected
        public static class SolrConnection {

            private String url;
            private Authentication authentication;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public Authentication getAuthentication() {
                return authentication;
            }

            public void setAuthentication(Authentication authentication) {
                this.authentication = authentication;
            }

            @ConfigurationProperties("authentication")
            @Introspected
            public static class Authentication {

                private boolean enabled;
                private String type;
                private String secret;
                private String issuer;
                private String subject;

                public boolean isEnabled() {
                    return enabled;
                }

                public void setEnabled(boolean enabled) {
                    this.enabled = enabled;
                }

                public String getType() {
                    return type;
                }

                public void setType(String type) {
                    this.type = type;
                }

                public String getSecret() {
                    return secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }

                public String getIssuer() {
                    return issuer;
                }

                public void setIssuer(String issuer) {
                    this.issuer = issuer;
                }

                public String getSubject() {
                    return subject;
                }

                public void setSubject(String subject) {
                    this.subject = subject;
                }
            }
        }
    }


}