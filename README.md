# search_indexer
(in progress)

Search Indexer is an open source search indexer meant to create a document indexer that scales out-of-the-box.  It's a work-in-progress aimed at the following milestones:

1. *End-to-end searching of wikipedia* - complete. Automatically download and install solr search engine.  Then it'll download all of wikipedia into solr.  Finally, it'll categorize each wiki article using OpenNLP category tagger.
2. *Scale wikipedia indexing with kafka* - in progress. Create the above steps using Spring cloud flow through 100% avro serialization/deserialization.  Create a generic document interface.
3. *Create multiple search engine indices* - not started.  Support for open search and an interface for other search engines.
4. *Allow for multiple document types to be indexed* - not started.  Move away from wikipedia and allow for more generic input of documents.
5. *Integrate pipeline steps for search to allow for dense vector calculations* - not started.

# End to end searching of wikipedia

If you have osx/linux, the current master branch will:

1) Download solr
2) Install solr / create wikipedia collection
3) download wikipedia articles
4) index all the wikipedia articles (took 6 hours total on a macbook air M2 2022)

# What is the purpose project?

This project is made to test a large practical set of data on a search engine to practice on improving a search experience.  As of now we're only dealing with wikipedia data.  

This is a maven project that's a self contained executiable made to install, hydrate, and create a full search engine for wikipedia.  The goal is to target java developers who are interested in learning more about advanced search features in solr.

I saw that solr now has dense vector indices - which is a new way to store and rank documents but has a far deeper learning curve than the keyword matching via BM25 algorithm that solr defaults to.  I'm creating this project to quickly index wikipedia so I can quickkly test multiple dense vector configurations.


Once I get the above completed, which should take a few months, I'd like to introduce other datasets:

* weather data
* city 311 data
* income tax data
* transportation data from major cities
* census data

Open for any suggestions.

