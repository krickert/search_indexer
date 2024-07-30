package com.krickert.search.indexer.solr.component;

import com.google.protobuf.Value;
import com.krickert.search.model.pipe.PipeDocument;


import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateHttp2SolrClient;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class SolrIndexingService {

    private static final Logger log = LoggerFactory.getLogger(SolrIndexingService.class);
    String urlArticleString = "http://localhost:8983/solr/wiki_article";
    String urlParagraphString = "http://localhost:8981/solr/wiki_paragraph";
    private ConcurrentUpdateHttp2SolrClient solrArticle;
    private final ConcurrentUpdateHttp2SolrClient solrParagraph;
    private final Integer numberOfWordsForSummary;
    private final int maxRetries;

    public SolrIndexingService(
            @io.micronaut.context.annotation.Value("${solr.min-words-for-paragraph}") Integer numberOfWordsForSummary,
            @io.micronaut.context.annotation.Value("${solr.max-retries}") int maxRetries) {
        this.maxRetries = maxRetries;
        solrArticle = createSolrClient(urlArticleString);
        this.solrParagraph = createSolrClient(urlParagraphString);
        this.numberOfWordsForSummary = numberOfWordsForSummary;

    }

    private ConcurrentUpdateHttp2SolrClient createSolrClient(String urlString) {
        return new ConcurrentUpdateHttp2SolrClient
                .Builder(urlString,
                new Http2SolrClient.Builder()
                        .withBasicAuthCredentials("solr","SolrRocks")
                        .withRequestTimeout(10000, TimeUnit.SECONDS)
                        .withMaxConnectionsPerHost(4000)
                        .withConnectionTimeout(10000, TimeUnit.SECONDS)
                        .build())
                .withQueueSize(500)
                .withThreadCount(6)
                .build();
    }

    public void addDocuments(List<PipeDocument> documents) {
        documents.stream().parallel().forEach(document -> {
            log.debug("got {} : {}", document.getId(), StringUtils.left(document.getTitle(), 50));
            addDocument(document);
        });
    }
    public void addDocument(PipeDocument document) {
        addArticleDocument(document);
        for (int i = 0; i < document.getBodyParagraphsCount(); i++) {
            try {
                addParagraphDocument(document, i);
            } catch (IndexingException e) {
                log.error("Indexing exception occurred.  Resetting the indexer.", e);
                synchronized (this) {

                    this.solrArticle = createSolrClient(urlArticleString);
                }
            }
        }
    }

    private void addParagraphDocument(PipeDocument document, int i) {
        addParagraphDocument(document, i, 0);
    }
    private void addParagraphDocument(PipeDocument document, int i, int retryNum) {
        if (!hasMoreThanNWordsAfterTitleInBody(
                document.getTitle(), document.getBodyParagraphs(i), numberOfWordsForSummary)
        ) {
            log.debug("skipping {} because it's too short", document.getBodyParagraphs(i));
            return;
        }
        SolrInputDocument doc = getSolrInputFields(document);
        doc.setField("id", createParagraphId(document, i));
        doc.setField("knn_vector", getParagraphVectorField(document, i));
        doc.setField("body", document.getBodyParagraphs(i));
        doc.addField("document_type", "article_paragraph");
        doc.addField("paragraph_num", i);
        try {
            solrParagraph.add(doc);
        } catch (SolrServerException | IOException e) {
            log.error("exception adding paragraph document, going to sleep and retry", e);
            try {
                Thread.sleep(retryNum * 5000L);
                if (retryNum > maxRetries) {
                    String partOfParagraph = StringUtils.left(document.getBodyParagraphs(i), 100);

                    throw new IndexingException("Problem adding paragraph for document id " + document.getId()
                    + " because there was an exception thrown.  The maximum number of retries, " + retryNum +
                            " was attempted and it failed.  Throwing the exception.  Body text: " + partOfParagraph, e);
                } else {
                    String partOfParagraph = StringUtils.left(document.getBodyParagraphs(i), 100);
                    log.warn("Problem adding paragraph for document id " + document.getId()
                            + " because there was an exception thrown.  The maximum number of retries, " + maxRetries +
                            " has not been hit so we'll continue to try " + (maxRetries - retryNum) + " times.  Godspeed " +
                            "to as and may we win this battle. Body text attempted: " + partOfParagraph, e);
                    addParagraphDocument(document, i, ++retryNum);
                }
            } catch (InterruptedException ex) {
                log.error("Strange situation where we slept too many times and / appliction stopped.");
                throw new RuntimeException(ex);
            }
        }

    }


    static boolean hasMoreThanNWordsAfterTitleInBody(String title, String body, int N) {
        try {
            if (title == null || title.isEmpty() || body == null || body.isEmpty()) {
                return false;
            }

            String bodyWithoutTitle = body.substring(title.length()).trim();
            String[] words = bodyWithoutTitle.split("\\s+");

            return words.length > N;
        } catch (StringIndexOutOfBoundsException aioobe) {
            log.error("exception adding paragraph document.\ntitle: {}\nbody:{}\nN:{}\nexception:", title, body, N, aioobe);
        }
        return false;
    }

    private String createParagraphId(PipeDocument document, int paragraphNum) {
        return document.getId() + "#" + paragraphNum;

    }

    private void addArticleDocument(PipeDocument document) {
        SolrInputDocument doc = getSolrInputFields(document);
        doc.setField("body", document.getBody());
        doc.setField("knn_vector", getVectorField(document));
        doc.addField("document_type", "article");
        try {
            solrArticle.add(doc);
        } catch (SolrServerException | IOException e) {
            log.error("exception adding " + document.getTitle(), e);
        }
    }

    @NotNull
    private SolrInputDocument getSolrInputFields(PipeDocument document) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", document.getId());
        doc.setField("url",  makeWikiURL(document));
        doc.setField("title", document.getTitle());
        doc.setField("revisionId", document.getRevisionId());
        return doc;
    }

    private static List<Float> getVectorField(PipeDocument document) {
        List<Value> values = document.getCustomData().getFieldsMap().get("embeddings").getListValue().getValuesList();
        List<Float> solrValues = new ArrayList<>();
        for (Value value : values) {
            solrValues.add((float)value.getNumberValue());
        }
        return solrValues;
    }

    private static List<Float> getParagraphVectorField(PipeDocument document, int i) {
        List<Value> values = document.getCustomData().getFieldsMap().get("paragraphEmbeddings").getListValue().getValuesList().get(i).getListValue().getValuesList();
        List<Float> solrValues = new ArrayList<>();
        for (Value value : values) {
            solrValues.add((float)value.getNumberValue());
        }
        return solrValues;
    }

    private String makeWikiURL(PipeDocument document) {
        return "https://en.wikipedia.org/?curid=" + document.getId();
    }

}
