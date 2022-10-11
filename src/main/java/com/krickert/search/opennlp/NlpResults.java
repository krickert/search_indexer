package com.krickert.search.opennlp;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class NlpResults implements Serializable {
    private Map<String, Collection<String>> results;

    public Map<String, Collection<String>> getResults() {
        return results;
    }

    public void setResults(Map<String, Collection<String>> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("results", results)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof NlpResults that)) return false;

        return new EqualsBuilder().append(results, that.results).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(results).toHashCode();
    }
}
