package com.krickert.search.wikipedia;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.krickert.search.opennlp.NlpExtractor;
import com.krickert.search.opennlp.NlpResults;
import com.krickert.search.opennlp.OrganizationExtractor;
import com.krickert.search.opennlp.PersonExtractor;
import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;
import opennlp.tools.tokenize.TokenizerME;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.wikiclean.WikiClean;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    public static final String SHORT_WIKI_URL = "short_wiki_url";
    public static final String BODY = "body";
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final AsyncSolrIndexerRunnable solrIndexerRunnable;
    private final BlockingQueue<SolrInputDocument> documents;
    private final OrganizationExtractor orgFinder;
    private final PersonExtractor personExtractor;
    private final TokenizerME tokenizer;
    private final NlpExtractor locationExtractor;
    private final NlpExtractor dateExtractor;
    Logger logger = LoggerFactory.getLogger(SolrArticleFilter.class);

    public static final DB db = DBMaker.fileDB("/Users/kristianrickert/nlp_store").executorEnable().transactionEnable().concurrencyScale(10).make();

    private static final HTreeMap<String, NlpResults> nlpMap = (HTreeMap<String, NlpResults>) db.hashMap("nlpMap").createOrOpen();

    public SolrArticleFilter(BlockingQueue<SolrInputDocument> documents,
                             AsyncSolrIndexerRunnable solrIndexer,
                             OrganizationExtractor orgFinder,
                             PersonExtractor personExtractor,
                             NlpExtractor locationExtractor,
                             NlpExtractor dateExtractor,
                             TokenizerME tokenizer) {
        this.documents = documents;
        executor.execute(solrIndexer);
        this.solrIndexerRunnable = solrIndexer;
        this.tokenizer = tokenizer;
        this.orgFinder = orgFinder;
        this.personExtractor = personExtractor;
        this.locationExtractor = locationExtractor;
        this.dateExtractor = dateExtractor;
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

        addNlpFields(page, plainWikiText, doc);

        return doc;
    }

    private void addNlpFields(WikiArticle page, String plainWikiText, SolrInputDocument doc) {
        final Map<String,Collection<String>> nlpResults = getNlpResults(page, plainWikiText, page.getId()).getResults();
        for(String key : nlpResults.keySet()) {
            doc.addField(key, nlpResults.get(key));
        }
    }
    public NlpResults getNlpResults(WikiArticle page, String plainWikiText, String pageId) {
        if (nlpMap.containsKey(pageId)) {
            logger.info("returning cached entry for {}", pageId);
            return nlpMap.get(pageId);
        }
        logger.info("pageId {} is not in cache for nlp data.  performing the work", pageId);
        Map<String,Collection<String>> results = Maps.newHashMapWithExpectedSize(4);

        String[] textTokens = tokenizer.tokenize(plainWikiText);
        String[] titleTokens = tokenizer.tokenize(page.getTitle());
        results.putAll(getOrganizationFields(textTokens, titleTokens));
        results.putAll(getPersonFields(textTokens, titleTokens));
        results.putAll(getLocationFields(textTokens, titleTokens));
        results.putAll(getDateFields(textTokens, titleTokens));

        NlpResults nlpResults = new NlpResults();
        nlpResults.setResults(results);
        logger.info("adding {}  to cache" , pageId);
        nlpMap.put(pageId, nlpResults);
        return nlpResults;
    }

    private Map<String,Collection<String>> getLocationFields(String[] textTokens, String[] titleTokens) {
        Map<String, Collection<String>> results = Maps.newHashMapWithExpectedSize(4);
        Collection<String> locationsInText  = locationExtractor.extract(textTokens);
        if (!CollectionUtils.isEmpty(locationsInText)) {
            results.put("body_location_nlp2", locationsInText);
        }
        Collection<String> locationTitle = locationExtractor.extract(titleTokens);
        if (!CollectionUtils.isEmpty(locationTitle)) {
            results.put("title_location_nlp2", locationTitle);
        }
        return results;
    }
    private Map<String,Collection<String>> getDateFields(String[] textTokens, String[] titleTokens) {
        Map<String, Collection<String>> results = Maps.newHashMapWithExpectedSize(4);
        Collection<String> datesinText = dateExtractor.extract(textTokens);
        if (!CollectionUtils.isEmpty(datesinText)) {
            results.put("body_dates_nlp2", datesinText);
        }
        Collection<String> datesInTitle = dateExtractor.extract(titleTokens);
        if (!CollectionUtils.isEmpty(datesInTitle)) {
            results.put("title_dates_nlp2", datesInTitle);
        }
        return results;
    }
    private Map<String,Collection<String>> getPersonFields(String[] textTokens, String[] titleTokens) {
        Map<String, Collection<String>> results = Maps.newHashMapWithExpectedSize(4);
        Collection<String> personsInText = personExtractor.extractPersons(textTokens);
        if (!CollectionUtils.isEmpty(personsInText)) {
            results.put("body_persons_nlp2", personsInText);
        }
        Collection<String> personsTitle = personExtractor.extractPersons(titleTokens);
        if (!CollectionUtils.isEmpty(personsTitle)) {
            results.put("title_persons_nlp2", personsTitle);
        }
        return results;
    }

    private Map<String,Collection<String>> getOrganizationFields(String[] textTokens, String[] titleTokens) {
        Map<String, Collection<String>> results = Maps.newHashMapWithExpectedSize(4);
        try {
            Collection<String> organizations = orgFinder.extractOrganizations(textTokens);
            if (!CollectionUtils.isEmpty(organizations)) {
                results.put("body_organizations_nlp2", organizations);
            }
            Collection<String> title_organizations = orgFinder.extractOrganizations(titleTokens);
            if (!CollectionUtils.isEmpty(title_organizations)) {
                results.put("title_organizations_nlp2", title_organizations);
            }
        } catch (IOException e) {
            logger.error("problem with extracting organizations for " + titleTokens, e);
        }
        return results;
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