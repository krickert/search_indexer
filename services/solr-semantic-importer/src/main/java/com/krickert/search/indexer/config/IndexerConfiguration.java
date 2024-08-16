package com.krickert.search.indexer.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Objects;

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
                private String clientSecret;
                private String clientId;
                private String issuer;
                private String issuerAuthId;


                public boolean isEnabled() {
                    return enabled;
                }

                public String getIssuerAuthId() {
                    return issuerAuthId;
                }

                public void setIssuerAuthId(String issuerAuthId) {
                    this.issuerAuthId = issuerAuthId;
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

                public String getClientSecret() {
                    return clientSecret;
                }

                public void setClientSecret(String clientSecret) {
                    this.clientSecret = clientSecret;
                }

                public String getClientId() {
                    return clientId;
                }

                public void setClientId(String clientId) {
                    this.clientId = clientId;
                }

                public String getIssuer() {
                    return issuer;
                }

                public void setIssuer(String issuer) {
                    this.issuer = issuer;
                }
            }
        }
    }


}