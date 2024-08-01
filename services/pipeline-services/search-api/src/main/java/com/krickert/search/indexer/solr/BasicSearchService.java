package com.krickert.search.indexer.solr;

import com.google.common.collect.Lists;
import com.krickert.search.vectorizer.Vectorizer;
import jakarta.inject.Singleton;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.solr.client.solrj.util.ClientUtils.escapeQueryChars;

@Singleton
public class BasicSearchService {
    private final Vectorizer vectorizer;
    private final SolrClient solrClient;

    public BasicSearchService(Vectorizer vectorizer, SolrClient solrClient) {
        this.vectorizer = vectorizer;
        this.solrClient = solrClient;
    }

    public SearchResults search(String query, SearchApiController.TYPE type) throws SolrServerException, IOException {
        return performSemanticQuery(solrClient, query, type);
    }

    private SearchResults performSemanticQuery(SolrClient client, String semanticQuery, SearchApiController.TYPE type) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");

        switch (type) {
            case ARTICLE_SEMANTIC, PARAGRAPH_SEMANTIC:
                semanticQuery(semanticQuery, query);
                break;
            case ARTICLE_KEYWORD, PARAGRAPH_KEYWORD, EVERYTHING_KEYWORD:
                keywordQuery(semanticQuery, query);
                break;
            case ARTICLE_RERANK, PARAGRAPH_RERANK:
                keywordQuery(semanticQuery, query);
                rerankQuery(semanticQuery, query);
                break;
        }
        // Set the query string.

        query.setShowDebugInfo(true);
        // Execute the query.
        QueryRequest req = new QueryRequest(query);
        req.setBasicAuthCredentials("solr", "SolrRocks");
        QueryResponse response = client.query(type.collection, query);
        // Get the results.
        return createSearchResultsFromSolrDocuments(response, semanticQuery);
    }

    private void rerankQuery(String semanticQuery, SolrQuery query) {
        //rq={!rerank reRankQuery=$rqq reRankDocs=4 reRankWeight=1}&rqq={!knn f=vector topK=10}[1.0, 2.0, 3.0, 4.0]

        query.add("rq", "{!rerank reRankQuery=$rqq reRankDocs=4 reRankWeight=3}");
        query.add("rqq", "{!knn f=knn_vector topK=10}"+ Arrays.toString(vectorizer.embeddings(semanticQuery)));
    }

    private static void keywordQuery(String semanticQuery, SolrQuery query) {
        query.addFilterQuery("title:" + escapeQueryChars(semanticQuery));
        query.addFilterQuery("body:" + escapeQueryChars(semanticQuery));
    }

    private void semanticQuery(String semanticQuery, SolrQuery query) {
        Collection<Float> embeddings =
                createEmbeddings(vectorizer.embeddings(semanticQuery));
        query.addFilterQuery("{!knn f=knn_vector topK=10}" + embeddings);
    }

    private SearchResults createSearchResultsFromSolrDocuments(QueryResponse queryResponse, String query){
        SolrDocumentList solrDocuments = queryResponse.getResults();
        List<SearchResult> results = Lists.newArrayListWithCapacity(solrDocuments.size());
        int i = 0;
        for(SolrDocument solrDocument : solrDocuments){
            String title = (String) solrDocument.getFieldValue("title");
            String url = (String) solrDocument.getFieldValue("url");
            String paragraph = (String) solrDocument.getFieldValue("body");
            int rank = ++i;
            String id = (String) solrDocument.getFieldValue("id");
            results.add(new SearchResult(id, title, url, paragraph, rank));
        }
        return new SearchResults(query, solrDocuments.getNumFound(), queryResponse.getQTime(), results);
    }

    private Collection<Float> createEmbeddings(float[] embeddings) {
        // Create an empty array list of floats.
        List<Float> returnVal = new ArrayList<>(embeddings.length);
        // Iterate over the primitive float array and add each element to the array list of floats.
        for (float f : embeddings) {
            returnVal.add(f);
        }
        return returnVal;
    }
}