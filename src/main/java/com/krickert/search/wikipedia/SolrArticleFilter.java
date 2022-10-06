package com.krickert.search.wikipedia;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikiclean.WikiClean;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SolrArticleFilter implements IArticleFilter {
    public static final String ID = "id";
    public static final String TEXT = "text";
    public static final String TITLE = "title";
    public static final String WIKIPEDIA_CATEGORY = "wikipedia_category";
    final static String xmlStartTag = "<text xml:space=\"preserve\">";
    final static String xmlEndTag = "</text>";
    private final static WikiClean cleaner = new WikiClean.Builder().withFooter(true).withTitle(false).build();
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final AsyncSolrIndexerRunnable solrIndexerRunnable;
    private final BlockingQueue<SolrInputDocument> documents;
    Logger logger = LoggerFactory.getLogger(SolrArticleFilter.class);

    public SolrArticleFilter(BlockingQueue<SolrInputDocument> documents, AsyncSolrIndexerRunnable solrIndexer) {
        this.documents = documents;
        executor.execute(solrIndexer);
        this.solrIndexerRunnable = solrIndexer;
    }

    private static String extractCleanTestFromWiki(WikiArticle page) {
        return cleaner.clean(xmlStartTag + page.getText() + xmlEndTag);
    }

    public void stopListeningForSolrUpdates() {
        solrIndexerRunnable.stopListening();
        executor.shutdown();
    }

    @Override
    public void process(WikiArticle page, Siteinfo siteinfo) {

        String plainWikiText = extractCleanTestFromWiki(page);
        logger.debug("Parsed data: {}", plainWikiText);
        if (plainWikiText.contains("REDIRECT")) {
            logger.debug("direct article, not indexing: {}", page.getTitle());
        } else {
            logger.info("adding {} to the queue", page.getTitle());
            documents.add(createSolrDocument(page, plainWikiText));
        }

    }

    private SolrInputDocument createSolrDocument(WikiArticle page, String plainWikiText) {
        SolrInputDocument doc = new SolrInputDocument();
        String title = page.getTitle();
        doc.addField(ID, page.getId());
        doc.addField(TEXT, plainWikiText);
        doc.addField(TITLE, page.getTitle());
        String categoryType = findWikiCategory(title);
        doc.addField(WIKIPEDIA_CATEGORY, categoryType);
        Collection<SolrInputDocument> urlsInPage = WikiURLExtractor.parseUrlEntries(page.getText(), page.getId());
        doc.addChildDocuments(urlsInPage);
        return doc;
    }

    private String findWikiCategory(String title) {
        if (title.startsWith("Category:")) {
            return ARTICLE_TYPE.CATEGORY.getWikiCategory();
        } else if (title.startsWith("List of")) {
            return ARTICLE_TYPE.LIST.getWikiCategory();
        } else if (title.startsWith("Wikipedia:")) {
            return ARTICLE_TYPE.WIKIPEDIA.getWikiCategory();
        } else if (title.startsWith("Draft:")) {
            return ARTICLE_TYPE.DRAFT.getWikiCategory();
        } else if (title.startsWith("Template:")) {
            return ARTICLE_TYPE.TEMPLATE.getWikiCategory();
        } else if (title.startsWith("File:")) {
            return ARTICLE_TYPE.FILE.getWikiCategory();
        } else {
            return ARTICLE_TYPE.ARTICLE.getWikiCategory();
        }
    }

    private enum ARTICLE_TYPE {
        CATEGORY("Category"),
        LIST("List"),
        DRAFT("Draft"),
        WIKIPEDIA("Wikipedia"),
        TEMPLATE("Template"),
        FILE("File"),
        ARTICLE("Article");

        final String categoryValue;

        ARTICLE_TYPE(String categoryValue) {
            this.categoryValue = categoryValue;
        }

        public String getWikiCategory() {
            return categoryValue;
        }
    }

}