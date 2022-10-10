package com.krickert.search.wikipedia;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Component
public class WikiURLExtractor {

    public static Collection<URLEntry> parseUrlEntries(String pageText, String pageId) {
        String[] urlElements = StringUtils.substringsBetween(pageText, "[http", "]");
        if (urlElements == null) {
            return Collections.emptyList();
        }
        ArrayList<URLEntry> urlEntries = Lists.newArrayListWithExpectedSize(urlElements.length);
        for (String urlElement : urlElements) {
            URLEntry entry = generateUrlEntry(urlElement);
            if (entry != null) {
                urlEntries.add(entry);
            }
        }
        return urlEntries;

    }


    private static URLEntry generateUrlEntry(String wikiCleanedText) {
        String[] entries = StringUtils.split(wikiCleanedText, " ", 2);
        if (ArrayUtils.isEmpty(entries)) {
            return null;
        }

        final String url;
        final String value;
        //just a URL
        url = "http" + entries[0];
        if (entries.length == 1) {
            value = StringUtils.EMPTY;
        } else {
            value = entries[1];
        }
        return new URLEntry(url, value);

    }

    public static class URLEntry {

        @Field("link")
        private final String URL;
        @Field("link_text")
        private final String text;

        public URLEntry(String url, String text) {
            URL = url;
            this.text = text;
        }

        public String getURL() {
            return URL;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            URLEntry urlEntry = (URLEntry) o;

            return new EqualsBuilder().append(URL, urlEntry.URL).append(text, urlEntry.text).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(URL).append(text).toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("URL", URL)
                    .append(" text", text)
                    .toString();
        }
    }


}
