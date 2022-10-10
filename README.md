# solr9_tutorial_data_village
Tutorial for search technologies - 

Right now this app automatically downloads solr, starts it, and creates a collection, downloads wikipedia, indexes it, and then the fun starts.  

# What is this project?

This project is made to test a large practical set of data on a search engine to practice on improving a search experience.  As of now we're only dealing with wikipedia data.  

This is a maven project that's a self contained executiable made to install, hydrate, and create a full search engine for wikipedia.  The goal is to target java developers who are interested in learning more about advanced search features in solr.

This is basically my sandbox for learning new things about search.  I'm sharing it in case anyone would like to test this sort of stuff with this package.

I'm using my own jfrog repo for some jars.  I've patched a few older projects and attempting to patch them accordingly.  So if it doesn't compile after downloading, this could be the issue.  Just write me and I can see about fixing it.

# How does it work
You should only need a java VM on your machine (I still need to figure out the minimum version), configure application.properties, and run it.  If all goes well, the following will happen:

1) Download solr
2) Install solr / create wikipedia collection
3) download wikipedia articles
4) index all the wikipedia articles (took 6 hours total on a macbook air M2 2022)

# How it was made
This application uses spring boot using standard dependency injection.  Each of the above tasks were coded and signaled to go on/off via configuration parameters.  It's been tested only on my macbook air so far, but I intend to test it on other PCs.

# Things to do next
* OpenNLP configuration in solr collection
* install elastic search as well
* install kafka 
* create solr and elastic search consumers

Other things to consider:
* Integrate a client to work on both search engines (avro, thrift, what else?)
* Create services to work with spacy and other snazzy python products

Other data sets are being considered such as:
* weather data
* city 311 data
* income tax data
* transportation data from major cities
* census data

Open for any suggestions.

