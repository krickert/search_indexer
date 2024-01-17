# search_indexer
Welcome to the search indexing project.  I've been working on search technologies for 15 years, and created this project to open source patterns I've seen in the industry that aren't easily done when doing searches.

So this this project indexes all of wikipedia. Next, it'll index web crawls.

Whenever I approach a new search project the biggest challenge always ends up being the indexing part and analytics.  Today's modern search engines - both new and enterprise - already solve all the "hard" problems of scaling, speed, and up-to-date top-of-the-line search and relevancy sorting technologies.  However, I have always had a hard time testing new technologies because it's simply hard to get a lot of documents in an index quickly. *Indexing is always the hardest part of search.*  

That's where this project comes in.  I started this to test semantic search on wikipedia data, but it's since evolved to be a full fledged indexing solution.

As of now, this project allows you to index all of wikipedia [directly from the wiki foundation's dump files](https://dumps.wikimedia.org/enwiki/).

The next iteration will feature a selenium-based web crawler.  This branch crawls using selemium on a chrome browser.  


## Abstract
This project sets out to:
* Allow data scientists quickly create access to popular text (i.e. all wikipedia articles) and structured data (IRS, 311, weather, census, public travel data) with little effort
* Make recrawling updates to the data sets fast and trouble-free
* Allow for quick enhancement of the data in multiple languages by use of a pluggable gRPC service
* Deploy software suite on a laptop or cloud
* Directly run the software suite on container-based technology or directly on any OSX, Windows, or Linux machine.
* Access the data through an advanced search engine with output in multiple formats (JSON, XML, or Google Protocol Buffers)

Most of my career has been in analytics or search technologies.  When working under these umbrellas, I don't spend much time testing the software as I do getting data into the new software.  This can include hours of time parsing files, crawling sites, writing SQL calls, etc.  

Fast forward to 2023, there is a large set of many data sources publicly available to perfrom the above tasks.  So this software is set to do exactly that: take public well-known data and parse it for consuming on other apps.

## Overview
This project is made to be an OOTB enterprise data pipeline for processing data from multiple 
sources and to be placed into a datastream for processing.  Processors can be written in most popular languages.

The data crawled is wikipeda right now but will expand to be multiple open data sources with reliable formats.  Additonally, a web and database crawler are possible.

The data processing steps from multiple sources which perform the following:
* Retrieval - grab the data from a data source and save it to a shared storage device.  As of now it only processes wikipedia data, but additional data will be added.
* Parsing - After the data is retrieved, the data will go into a parsing step which will convert it to a plain text document for text processing.
* Enrichment - Now that the document is plain text, it will be applied to a pipeline which would execute multiple services to enrich the document.
* Sink - Once the data is fully enriched, it will go into a data sink to output to the desired service such as a search engine, vector store, or a data scientist experiementing with data.


## Technical architecture
### Components used
* Protocol buffers - the data format of all data in the stream
* Kafka - the messaging queue 
* gRPC - the service layer for document enrichment.  Can be any gRPC service in any supported language.
* Consul - gRPC and REST service registration
* OpenJDK 17  - Base layer for project
* Micronaut - Dependency injection layer
* Selenium - web crawling via web driver
* Jobrunr - open source job running solution with a micronaut hook
* mongodb - to store crawl data and temporary data not suited for kafka
* There's many more, we'll add them here later

### Data flows
#### Overall data flow - wikipedia
![cartoon for managers](/docs/arch_diagrams/search_indexer-StreamFlow.drawio.svg)

The above demonstrates the following flow:
* Document Retrieval
* Document Parsing
* Document Enrichment 

##### Document Retrieval 
The data is sent to a repository for raw data processing.  
##### Document parsing
Take the document and parse it into plain text.
##### Document enrichment
Take the document and add some feature.

#### Wikipedia Document Retrieval
![document retrieval](/docs/arch_diagrams/search_indexer-WikiRetrieval.drawio.svg)

The document retrieval does the following steps to ensure:
* The latest version of the documents are retrieved
* if the latest version is not intended, the user can configure it to a specific dump date
* user configures how many downloads would happen simultaneously in the settings.  Wikimedia allows up to 3 at a time.
* all downloads are validated by their md5 sum
* downloads are going to be stored onto disk.  In the future s3 buckets would be supported.

##### Next document retrievals planned
* Web crawler integration such as [heritix](https://github.com/internetarchive/heritrix3) or [scrapy](https://scrapy.org/) integration
* JDBC crawler
* 311 data from various open APIs and data dumps
* IRS data
* Weather data
* Map data

#### Wiki Document parsing
The parsing only removes wiki data and adds the metadata returned from the dumps.  Current types of wiki documents from the Wikipedia dumps that are supported go as follows:
* ARTICLE 
* CATEGORY
* LIST
* DRAFT
* WIKIPEDIA
* TEMPLATE
* FILE 
* REDIRECT

#### Pipeline Processing

##### Overview

![Pipeline Processor Flow](/docs/arch_diagrams/search_indexer-PipelineProcessorFlow.drawio.svg)

Once the parsed document is parsed, it is cleaned up, the pipeline processing step can enhance the document by applying a pipeline step to the document.

This is a set of services all with the same gRPC interface which simply inputs a Pipeline Document and outputs the same.  The service can enhance, read, or manipulate the document through the series of pipelines.

As of now the project has two pipeline steps: a vectorizer and an NLP named entity rocognition service.  They're both a reference point as to how to create a gRPC service to enhance the document.  Further implementations of gRPC services in multiple languages are planned.

##### Setting up the pipeline processor

The pipeline processor project is in `services/pipeline-processor`.  This application:
1. Takes in a docuemnt from kafka.
   1. The type is `PipeDocument` which is a google protocol buffer type, defined in `protobuf-projects/wikisearch-model/src/main/protobuf/pipes.proto`
   2. The topic is `pipe-document`.  The application subscribes to this topic.
2. Processes the document through a grpc pipeline of services registered in consul.
   1. In the `application.yml` file, you can configure the pipeline stages that would process.  These stages are all grpc services that take in the same interface but different implementations meant to enhance a document.  Instructions on how to do this is below.
   2. When the application starts up, when a document is consumed from kafka, the processor will run the document through the configured pipelines defined in the application.yml file.
   3. The result of this file gets saved into a kafka topic called `enhanced-pipeline-document` which is also a PipeDocument type.

There are two projects that can be used to test with consul: `services/grpc-nlp-service` and `services/grpc-vectorizor-service`.  Each are standalone applications that will run on 2 different ports on your machine.  These services are configured in the application.yml file to automatically register for a consul server.  

###### Configuration

The following configuration parameters need to be registered in `application.yml`:
1. Consul server zone
2. Grpc service list registered in consul
3. Kafka configuration

###### Consul setup
In the `pipeline-document`'s `application.yml` file:
```
consul:
  client:
    registration:
      enabled: false (1)
    defaultZone: ${CONSUL_HOST:localhost}:${CONSUL_PORT:8500} (2)
    discovery:
      enabled: true (3)
grpc:
  client:
    plaintext: true (4)
    discovery:
      enabled: true (5)

kafka:
  enabled: true (6)

pipeline-config:
  client-configs: (7)
    nlp2:
      type: server_grpc
      host: "localhost"
      port: "50051"
    nlp:
      type: consul_grpc
      consul_service: nlp
    vectorizer:
      type: consul_grpc
      consul_service: vectorizer

  pipeline-processor: (8)
    test-pipeline: (9)
      services:
        - nlp
        - vectorizer
    search-pipeline:
      services:
        - vectorizer
    datascience-pipeline:
      services:
        - nlp
```
Note: *client-configs* has not yet been implemented.  So, for now, the `client-configs` parameter can be ignored.  This is going to be refactored in the future.  The consul services would not need to be added to the client-config section in the near future as the app will assume the service name is the reference to the grpc service in consul.

The above is referenced with the following instructions for the config:
1. `consul.client.registration` should be false.  Soon this will turn to true as the pipeline-processor itself will offer a grpc service to process a specific pipeline processor.  However, if this is set to true, there will be no harm as the app will still run.
2. `defaultZone` contains the consul server configuration.  Eventually, all the configuration will be in consul as an option.
3. `consul.client.discovery` needs to be true.  It's the feature of micronaut that allows for the dynamic registation of a `PipeDocument` processor.
4. `grpc.client.plaintext` is only true because the 2 services that come as an example are also serving as plantext.  However, setup your ssl per the directions in micronaut grpc if you don't want to use plaintext.
5. `grpc.client.discovery.enabled` needs to be set to true in order to get the grpc channel from consul.
6. `kafka.enabled` is set to true.  Please read the kafka instructions if you're not using standard ports, etc for the kafka server.
7. `pipeline-config.client-config` feature is not yet implemented.   Right now this app assumes consul registered grpc services only.
8. `pipeline-config.pipeline-processor` sets up different named pipelines for the registered grpc consul services.  All grpc services must implement the PipeService interface for gRPC and registered in consul as a service.
9. `pipeline-config.pipeline-processor.*.services` the wildcard is the name of the defined pipeline for the `PipeDocument` processing.


## install directions

The docker directory has the latest confluent platform. It also contains a script that automatically creates all the
needed topics for this application.

(in progress)

Search Indexer is an open source search indexer meant to create a document indexer that scales out-of-the-box. It's a
work-in-progress aimed at the following milestones:

1. *End-to-end searching of wikipedia* - complete. Automatically download and install solr search engine. Then it'll
   download all of wikipedia into solr. Finally, it'll categorize each wiki article using OpenNLP category tagger.
2. *Scale wikipedia indexing with kafka* - completed. Create the above steps using Spring cloud flow through 100% avro
   serialization/deserialization. Create a generic document interface.
3. *Create multiple search engine indices* - not started. Support for open search and an interface for other search
   engines.
4. *Allow for multiple document types to be indexed* - not started. Move away from wikipedia and allow for more generic
   input of documents.
5. *Integrate pipeline steps for search to allow for dense vector calculations* - not started.

# Changes

1. Moved to protocol buffers instead of avro. The IDL for Protocol buffers is more mature, and the OOTB nature to
   integrate it with gRPC makes this a solid choice.
2. The above chart does work
3. Protocol buffers are used as the value for all the kafka topics.
4. Taking advantage of the automatic dockerized kafka during unit testing. Added multiple tests that test the
   serialization.

# things to do

* learn more about kafka topics. About 1/2 way through some of the books, not jumping into any of the super fancy stuff
  yet.
* figure out if we will use key/value storage with KTable? I feel odd moving away from the avro binary to json,
* get a pipeline arch going which will allow for registering multiple pipeline services using gRPC. The idea is to
  create a templated request service and the end user can create the service which is "registerd" in the application yml
  of the pipeline project.

# things to cache/store outside of the pipeline

* NLP cache by revision ID key to reduce the cost of processing
* create a feature that doesn't parse the articles outside of the raw stage if it's already in the db and marked
  processed
* find a pluggable way to add enhancements to the document. probably just by tagging and a revision ID along with some
  more metadata. trying to avoid doing a SQL database and if I do, consider using the sql dumps from wikimedia. But I
  want to keep this search-centric and not use that model.
* start a document store once the document is cleaned. can just use a PipeDocument topic for now?
* once we get this into solr, create https://vespa.ai/ search engine
* also consider using weaviate

# categories listed

a ton of documents cause redirects. start seeing how we can get that data because the wikiparser doesn't seem to give
the article id when redirecting. It'll be cool to save that data because it's an amazing source of similarity stuff to
do with the dense vectors.

# What is the purpose project?

This project is made to test a large practical set of data on a search engine to practice on improving a search
experience. As of now we're only dealing with wikipedia data.

This is a maven project that's a self contained executiable made to install, hydrate, and create a full search engine
for wikipedia. The goal is to target java developers who are interested in learning more about advanced search features
in solr.

I saw that solr now has dense vector indices - which is a new way to store and rank documents but has a far deeper
learning curve than the keyword matching via BM25 algorithm that solr defaults to. I'm creating this project to quickly
index wikipedia so I can quickkly test multiple dense vector configurations.

Once I get the above completed, which should take a few months, I'd like to introduce other datasets:

* weather data
* city 311 data
* income tax data
* transportation data from major cities
* census data

Open for any suggestions.

h1. Platform Dependent Notes


I'm building this as a linux build, but often code on OSX and Windoze.  Therefore, you can expect this software to likely work on a linux box running on an intel chipset.

Since the pytorch library is platform-dependent, there are a number of things that we had to wrangle to get this to work right between the various platforms.  Even then, I can't promise this would work natively out-of-the-box unless you're using my docker images.

h2. Windows Notes
when coding on Windoze, I use the WSL2 library.  I wouldn't recommend doing this to anyone at this time becuase the grpc services take advantage of GPUs, and I'm not sure if the windoze machines can do that.  Regardless, it should work and run on a windows machine.  If you cna't get it to run, send me a note or open up an issue and I'll be glad to fix it ASAP.


h2. Linux
I have an nvidia 2070 RTX, and I installed the following libraries on my ubuntu server:
* Docker
* JDK 17 (JDK 21 doesn't seem to work due to an incompatibility with Mockito at this time)
* maven 3.9.5
* nvidia-cuda-toolkit
* nvidia-cudnn

The build is working on the autobuild for github, but I cannot promise full compatibility without using the docker images I'm pushing to docker regestry.

Protocol buffers should work OOTB without installing.  Maven handles downloading the native executable.

h3. OSX

build works on OSX, but I'd recommend running on native linux instead.  It seems to be at least 50% faster on a linux machine.

That being said, if you have:
* 3.9.5 maven
* JDK 17 (amazon)
* Docker Desktop

All running on OSX, I suspect this should work.

I'm attempting to put the non-CPU intensive services onto a swarm of raspberry pis in my office.  So I'll likely make both ARM64 and AMD64 docker images.  However, I'm going to start with intel images for now.
