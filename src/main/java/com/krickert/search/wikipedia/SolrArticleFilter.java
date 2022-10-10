package com.krickert.search.wikipedia;

import com.google.common.collect.Lists;
import com.krickert.search.opennlp.OrganizationExtractor;
import com.krickert.search.opennlp.PersonExtractor;
import edu.stanford.nlp.quoteattribution.Person;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.wikiclean.WikiClean;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SolrArticleFilter implements IArticleFilter {
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String WIKIPEDIA_CATEGORY = "wikipedia_category";
    final static String xmlStartTag = "<text xml:space=\"preserve\">";
    final static String xmlEndTag = "</text>";
    private final static WikiClean cleaner = new WikiClean.Builder().withFooter(true).withTitle(false).build();
    public static final String ORGANIZATIONS_NLP_2 = "organizations_nlp2";
    public static final String TITLE_ORGANIZATIONS_NLP_2 = "title_organizations_nlp2";
    public static final String SHORT_WIKI_URL = "short_wiki_url";
    public static final String BODY = "body";
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final AsyncSolrIndexerRunnable solrIndexerRunnable;
    private final BlockingQueue<SolrInputDocument> documents;
    private final OrganizationExtractor orgFinder;
    private final PersonExtractor personExtractor;
    private final TokenizerModel tokenModel;
    Logger logger = LoggerFactory.getLogger(SolrArticleFilter.class);

    public SolrArticleFilter(BlockingQueue<SolrInputDocument> documents,
                             AsyncSolrIndexerRunnable solrIndexer,
                             OrganizationExtractor orgFinder,
                             PersonExtractor personExtractor,
                             TokenizerModel tokenModel) {
        this.documents = documents;
        this.orgFinder = orgFinder;
        executor.execute(solrIndexer);
        this.solrIndexerRunnable = solrIndexer;
        this.tokenModel = tokenModel;
        this.personExtractor = personExtractor;
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
        doc.addField(BODY, plainWikiText);
        doc.addField(TITLE, page.getTitle());
        String categoryType = findWikiCategory(title);
        doc.addField(WIKIPEDIA_CATEGORY, categoryType);
        doc.addField(SHORT_WIKI_URL, "https://enwp.org/?curid=" + page.getId());
        addReferenceFields(page, doc);

        TokenizerME tokenizer = new TokenizerME(tokenModel);
        String[] textTokens = tokenizer.tokenize(plainWikiText);
        String[] titleTokens = tokenizer.tokenize(page.getTitle());
        addOrganizationFields(textTokens, titleTokens, doc);
        addPersonFields(textTokens, titleTokens, doc);
        return doc;
    }

    private void addPersonFields(String[] textTokens, String[] titleTokens, SolrInputDocument doc) {
        Collection<String> personsInText = personExtractor.extractPersons(textTokens);
        if (!CollectionUtils.isEmpty(personsInText)) {
            doc.addField("persons_text_nlp2", personsInText);
        }
        Collection<String> personsTitle = personExtractor.extractPersons(titleTokens);
        if (!CollectionUtils.isEmpty(personsTitle)) {
            doc.addField("persons_title_nlp2", personsTitle);
        }
    }

    private void addOrganizationFields(String[] textTokens, String[] titleTokens, SolrInputDocument doc) {
        try {
            Collection<String> organizations = orgFinder.extractOrganizations(textTokens);
            if (!CollectionUtils.isEmpty(organizations)) {
                doc.addField(ORGANIZATIONS_NLP_2, organizations);
            }
            Collection<String> title_organizations = orgFinder.extractOrganizations(titleTokens);
            if (!CollectionUtils.isEmpty(title_organizations)) {
                doc.addField(TITLE_ORGANIZATIONS_NLP_2, title_organizations);
            }
        } catch (IOException e) {
            logger.error("problem with extracting organizations for " + titleTokens, e);
        }
    }

    private void addReferenceFields(WikiArticle page, SolrInputDocument doc) {
        Collection<WikiURLExtractor.URLEntry> urlsInPage = WikiURLExtractor.parseUrlEntries(page.getText(), page.getId());
        if (!CollectionUtils.isEmpty(urlsInPage)) {
            Collection<String> urls = extractUrls(urlsInPage);
            doc.addField("reference_urls", urls);
            Collection<String> urlLinks = extractUrlRefLinks(urlsInPage);
            doc.addField("reference_html_links", urlLinks);
            Collection<String> urlDescriptions = extractUrlDescriptions(urlsInPage);
            if (!CollectionUtils.isEmpty(urlDescriptions)) {
                doc.addField("reference_url_descriptions", urlDescriptions);
            }
        }
    }

    private Collection<String> extractUrlDescriptions(Collection<WikiURLExtractor.URLEntry> urlsInPage) {
        List<String> returnVal = Lists.newArrayListWithExpectedSize(urlsInPage.size());

        for (WikiURLExtractor.URLEntry urlEntry : urlsInPage) {
            if (!StringUtils.isEmpty(urlEntry.getText())) {
                returnVal.add(urlEntry.getText());
            }
        }
        return returnVal;
    }

    private Collection<String> extractUrlRefLinks(Collection<WikiURLExtractor.URLEntry> urlsInPage) {
        List<String> returnVal = Lists.newArrayListWithExpectedSize(urlsInPage.size());

        for (WikiURLExtractor.URLEntry urlEntry : urlsInPage) {
            final String hrefText;
            if (StringUtils.isEmpty(urlEntry.getText())) {
                hrefText = urlEntry.getURL();
            } else {
                hrefText = urlEntry.getText();
            }
            returnVal.add("<a herf=\"" + urlEntry.getURL() + "\">" + hrefText + "</a>" );
        }
        return returnVal;
    }

    private Collection<String> extractUrls(Collection<WikiURLExtractor.URLEntry> urlsInPage) {
        List<String> returnVal = Lists.newArrayListWithExpectedSize(urlsInPage.size());

        for (WikiURLExtractor.URLEntry urlEntry : urlsInPage) {
            returnVal.add(urlEntry.getURL());
        }
        return returnVal;
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