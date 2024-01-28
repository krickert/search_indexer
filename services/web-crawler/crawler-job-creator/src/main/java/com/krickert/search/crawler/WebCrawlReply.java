package com.krickert.search.crawler;

public class WebCrawlReply {
    final String url;
    final String title;
    final String body;
    final String html;

    private WebCrawlReply(Builder builder) {
        this.url = builder.url;
        this.title = builder.title;
        this.body = builder.body;
        this.html = builder.html;
    }

    public static class Builder {
        private String url;
        private String title;
        private String body;
        private String html;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder html(String html) {
            this.html = html;
            return this;
        }

        public WebCrawlReply build() {
            return new WebCrawlReply(this);
        }
    }
}