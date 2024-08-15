package com.krickert.search.indexer.solr;

import org.apache.solr.common.SolrInputDocument;

import java.util.Collection;

public class HttpSolrSelectResponse {
    private final Long numFound;
    private final Long qtime;
    private final Long start;
    private final Collection<SolrInputDocument> docs;
    private final Long pageSize;

    private HttpSolrSelectResponse(Builder builder) {
        this.numFound = builder.numFound;
        this.qtime = builder.qtime;
        this.start = builder.start;
        this.docs = builder.docs;
        this.pageSize = builder.pageSize;
    }

    public Long getNumFound() {
        return numFound;
    }

    public Long getQtime() {
        return qtime;
    }

    public Collection<SolrInputDocument> getDocs() {
        return docs;
    }

    public Long getStart() {
        return start;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public static class Builder {
        private Long numFound;
        private Long qtime;
        private Long start;
        private Collection<SolrInputDocument> docs;
        private Long pageSize;

        public Builder numFound(Long numFound) {
            this.numFound = numFound;
            return this;
        }

        public Builder qtime(Long qtime) {
            this.qtime = qtime;
            return this;
        }

        public Builder docs(Collection<SolrInputDocument> docs) {
            this.docs = docs;
            return this;
        }

        public Builder start(Long start) {
            this.start = start;
            return this;
        }

        public Builder pageSize(Long pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public HttpSolrSelectResponse build() {
            return new HttpSolrSelectResponse(this);
        }
    }
}