# RAG engine powered by wikipedia data
This project is made to provide a full-fledged search engine and RAG for all wikipedia data.
It's your portable genius and FULLY 
open source—no amazon bill and can run on your laptop if it can handle the load.

If you want to help with this, let me know.
## Project Structure
This project is split up into multiple repositories.  Each project below is a standalone project and acts as a single microservice.

Unlike other RAG projects that try to have a one-stop-shop for a complex ecosystem, each component below operates on a protocol buffer 
contract that would run on either kafka for async processing or grpc for sync processing.
### List of projects with links to them
Below is a summary of each project:

| Project                                                                                                          | Description                                                                                                                                  | RAG step                 | Status                                                                          |
|:-----------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------|:--------------------------------------------------------------------------------|
| [rag-models](https://github.com/krickert/rag-models)                                                             | All the google protocol buffers and gRPC definitions.  Also has test tools with a base set of test data to use in unit or integration tests. | Modeling                 | Completed.  Clean up  and separating concerns needed.                           |
| [pipeline-processor](https://github.com/krickert/pipeline-processor)                                             | Simple pipeline processor that takes a set of services and enhances the PipeDocument                                                         | Pipeline Processing      | Completed, continual improvements and a front end are needed                    | 
| [chunked](https://github.com/krickert/chunker)                                                                   | Basic chunking services.  Will be extend for more semantic-style chunking.                                                                   | Chunking                 | Supports basic chunking by size and overlap.                                    |
| [nlp-ner](https://github.com/krickert/nlp-ner)                                                                   | Basic named entity recognition service                                                                                                       | Pipeline processing step | Uses open NLP and standford NLP.  Other implementations planned                 |
| [vectorizer](https://github.com/krickert/vectorizer)                                                             | pyTorch java implementation of sentence embedding                                                                                            | Pipeline processing step | Supports multiple LLM models.  Automatically downloads pytorch per your machine | 
| [markdown-parser](https://github.com/krickert/markdown-parser)                                                   | basic parser for markdown documents                                                                                                          | Parser                   | Needs to add more features.  Supports body and titles.                          |
| [tika-parser](https://github.com/krickert/tika-parser)                                                           | Proxies the newest TIKA parser to process PipeDocuments                                                                                      | Parser                   | needs to better integrate with tika-pipes                                       |
| [search-api](https://github.com/krickert/search-api)                                                             | semantic/vector search engine for solr.                                                                                                      | search                   | going to tie in with the indexing strategy to push searching                    |
| [solr-semantic-importer](https://github.com/krickert/solr-semantic-importer)                                     | indexes solr from either another solr collection or a Protocol buffer document                                                               | Indexing                 | will support more indexing connections soon, including pipedocuments            |
| [crawler-job-creator](https://github.com/krickert/crawler-job-creator)                                           | Part of the selenium-based web crawling solution                                                                                             | fetching                 | In progress.                                                                    |
| [crawler-manager](https://github.com/krickert/crawler-manager)                                                   | manages crawling jobs.                                                                                                                       | fetching                 | in progress, incomplete                                                         |
| [wiki-download-request-creator](https://github.com/krickert/wiki-download-request-creator)                       | goes to wikimedia latest list of wikipedia dump and creates download requests for processing                                                 | fetching                 | complete                                                                        |                                                                      |
| [wiki-download-dump-file-processor](https://github.com/krickert/wiki-download-dump-file-processor)               | Takes a download request and downloads the wikipedia article.                                                                                | fetching                 | complete                                                                        |
| [wiki-dump-file-to-wiki-article-processor](https://github.com/krickert/wiki-dump-file-to-wiki-article-processor) | takes a wiki dump file and parses wikipedia articles to plain text                                                                           | parsing                  | complete                                                                        |
| [wiki-article-to-pipedocument-processor](https://github.com/krickert/wiki-article-to-pipedocument-processor)     | takes a wiki article and maps it to a pipedocument                                                                                           | indexing                 | completed                                                                       |

### Structure of this project
This project uses the above projects as submodules and is made to create full releases. 

The major advantage of this project is that it has a scaffolding which follows an ability to quickly deploy to multiple clouds, run 
 locally, etc. while still supporting OOTB security standards common in large organizations.

Each of the subprojects is standalone microservices.  When possible, the following features are supported:
* Java projects can be run via a container or directly through java
* Output is supported via Java API, REST interface, gRPC interface
* Support for kafka for async processing
* Support for consul for auto discovery
* SSL/Authentication (Basic / JWT/ Oauth / Okta supported)
* Log systems like dynatrace, cloudwatch, and grafana supported

# Why build this Wiki RAG
* Gain hands-on experience as to how to build an enterprise pipeline.
* Wiki data is well-structured and curated as accurate, so it makes a great input to augment and test large language models in your system.
* You can try multiple search engines fast and evaluate their impact on a final output

# I want a RAG fast, why not just [Bedrock](https://aws.amazon.com/bedrock/) or [Google Vertex](https://cloud.google.com/vertex-ai?hl=en)?
## This might not be a bad idea
This is a great point, and there are a lot of solutions that work well for these products—especially if your organization allows the 
use of a third party internet LLM like Gemini.
There are a lot of situations where this is a far better solution—but you have to know 
what you're looking at before you decide which is the better approach. 
## This wiki indexer will teach you the fine line of those complexities 
By learning RAG with this project, you will understand the components that work within it.
It will teach you which 
features to look for when selecting a RAG solution—a task that can take at least months of research.
## Off the shelf solution discussion
### Third party solutions are expensive to test, and may not be a good solution anyway
While solutions offered by Amazon, Google, Microsoft, and other tech giants can be great for getting a chatbot up and running quickly, they might not be the best fit for certain enterprise-scale applications. 

Here are a few reasons why:
* __Customization__: In-house, self-built solutions can be tailored exactly to the needs of the enterprise. This level of customization might not be possible with off-the-shelf solutions.
* __Ownership and Control__: With a self-built solution, you have full control over your data, and how it is stored and processed. This can 
  be important for compliance with certain regulations.
* __Scaling and Performance__: While big tech companies offer decent scalability, there might be specific performance considerations within an enterprise that can be better addressed with a custom solution.
* __Cost__: While using a managed solution can reduce upfront costs, the ongoing costs can increase significantly with increasing usage. In 
  the long run, an in-house solution can cost less.

### When can a third party solution work?
That being said, turnkey solutions are constantly evolving, and they can still be a good choice for many organizations, depending on 
their specific needs and resources. 

It also comes down to the level of expertise- and resources-available within an organization to develop, maintain, and scale a custom 
solution.

In conclusion, it's all about finding the right balance between the specific needs of your organization, the advantages of turnkey solutions, and the resources available for a custom-built solution. 

A possible approach could be to start with a turnkey solution to understand the requirements and constraints,
and then gradually move to a custom solution as needs grow and become more specific.

But this project gets your hands dirty on day 1—you get a large corpus of semi-structured curated factual data gigabytes in size.  

It'll help answer if you're biting off more than you can chew.  

# Things this project does that will be part of a book

1. **Understanding of RAG**: You would gain a solid understanding of RAG (Retrieval-Augmented Generation) engines, which are crucial 
for modern AI applications. 
2. **Big Data Processing**: Through the Kafka pipeline and data loading process, learn about handling big data and the challenges associated with it. 
3. **Microservices Architecture**: The use of Docker containers illustrates the advantages and design principles of a microservices architecture. 
4. **Distributed System Scaling**: Exploring how this and similar architectures can run on a single laptop or scale to a much larger 
   infrastructure.  You would be exposed to concepts of distributed systems and scaling. 
5. **Deployment and Orchestration**: Exploring Docker.io and the use of Docker-compose files would equip readers to handle application deployment and orchestration. 
6. **End-to-End Testing**: Given the project's integration with Testcontainers, readers would learn the importance of and methods for comprehensive end-to-end testing.
7. **Knowledge in Different Cloud Platforms**: They would gain knowledge about the offerings of Amazon, Google, and Microsoft, making it 
   easier to make informed decisions about cloud services. 
8. **Building a Custom Solution vs. Using Turnkey Solutions**: The readers would learn about the trade-offs between custom models and turnkey solutions, like those offered by big tech companies. 
9. **Hands-On Experimentation**: The project would provide a concrete, real-world application to experiment with, complementing the theoretical learning. 
10. **Navigating Project Obstacles**: Lessons drawn from this project can contain wisdom about dealing with non-technical challenges of 
    software projects, like convincing stakeholders and managing varying levels of technical literacy.



# Technical steps this project does now
What this project does:
1. Grabs data
    1. Download wiki dumps from wikimedia.org
    2. Parses said files parallel using a kafka set of topics
    3. Serializes data into a protocol buffer format
2. Parses
    1. Turns the wiki text into pain text for processing
    2. Chunks the data
3. Enhances
    1. Creates vectors for said data (with a custom hugging face model downloaded by the DJL library created by Amazon)
    2. Performs an NLP Named Entity Recognition on any field for further processing within the wiki text
    3. Provide a custom pipeline for any other steps in between
4. Searches
   1. Saves said data into a search engine for a semantic search engine (Solr supported, little work to use OpenSearch)
5. Queries
   1. Performs vector queries on search engine

TODO
1. Use query results for context into a RAG (complete)
2. Create interfaces for RAG Prompt and output (complete)
3. Write 2–3 implementations of prompt (not started)
4. Write a front end (not started, using swagger-ui)
5. Utilize a job runner (in progress)
6. Create a web crawler (in progress)
7. Integrate an open source web analytics solution to the front end (in progress)


Welcome to the Wiki RAG project.  Please note that this project is not part of the wikimedia foundation, but they've been great at helping me get this framework to where it is now.

I've been working on search technologies for 15 years, and created this project to release code patterns I've seen in the industry that aren't easily done when testing new search technologies.

The biggest problem I've seen with search is indexing—the search portion is about 10% of a project.
Wronging data into the engine is where most of the efforts are drawn.

So if you want to test a new search engine -
you need a ton of data that can easily be ingested with the document already enhanced, then this is what you'd want.

Right now the project just indexes wikipedia.  I'm working on integrating the tika parser into the pipeline with a web crawler.  This would allow for document enrichment for web crawling.

Whenever I approach a new search project, the biggest challenge always ends up being the indexing part and analytics.
Today's modern search engines - both new and enterprise - already solve all the "hard"
problems of scaling, speed, and up-to-date top-of-the-line search and relevancy sorting technologies.
However, I have always had a hard time testing new technologies because it's simply hard to get a lot of documents in an index quickly.
Indexing is always the hardest part of search.*  

That's where this project comes in.
I started this to test semantic search on wikipedia data, but it's since evolved to be a full-fledged indexing solution.

As of now, this project allows you to index all of wikipedia [directly from the wiki foundation's dump files](https://dumps.wikimedia.org/enwiki/).

The next iteration will feature a selenium-based web crawler.  This branch crawls using selenium on a Chrome browser.  


## Abstract
This project sets out to:
* Allow data scientists to quickly create access to a popular text (i.e., all wikipedia articles) and structured data (IRS, 311, weather, census, public travel data) with little effort
* Make recrawling updates to the data sets fast and trouble-free
* Allow for quick enhancement of the data in multiple languages by use of a pluggable gRPC service
* Deploy software suite on a laptop or cloud
* Directly run the software suite on container-based technology or directly on any OSX, Windows, or Linux machine.
* Access the data through an advanced search engine with output in multiple formats (JSON, XML, or Google Protocol Buffers)

Most of my career has been in analytics or search technologies.
When working under these umbrellas, I spend little time testing the software as I do getting data into the new software.
This can include hours of time parsing files, crawling sites, writing SQL calls, etc.  

Fast-forward to 2024, there is a large set of many data sources publicly available to perform the above tasks.
So this software is set to 
do exactly that: take public well-known data and parse it for consuming on other apps.

## Overview
This project is made to be an OOTB enterprise data pipeline for processing data from multiple 
sources and to be placed into a datastream for processing.
Processors can be written in the most popular languages.

The data crawled is wikipeda right now but will expand to be multiple open data sources with reliable formats.
Additionally, a web and database crawler are possible.

The data processing steps from multiple sources which perform the following:
* Retrieval—grab the data from a data source and save it to a shared storage device.  As of now, it only processes wikipedia data, but additional data will be added.
* Parsing - After the data is retrieved, the data will go into a parsing step which will convert it to a plain text document for text processing.
* Enrichment—Now that the document is plain text, it will be applied to a pipeline which would execute multiple services to enrich the document.
* Sink—Once the data is fully enriched, it will go into a data sink to output to the desired service such as a search engine, vector store, or a data scientist experimenting with data.


## Technical architecture
### Components used
* Protocol buffers—the data format of all data in the stream
* Kafka - the messaging queue 
* gRPC—the service layer for document enrichment.  Can be any gRPC service in any supported language.
* Consul—gRPC and REST service registration
* OpenJDK 17
* - Base layer for a project
* Micronaut—Dependency injection layer
* Selenium—web crawling via a web driver
* Jobrunr—open source job running solution with a micronaut hook
* mongodb—to store crawl data and temporary data not suited for kafka
* There are many more, we'll add them here later

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
Take the document and add some features.

#### Wikipedia Document Retrieval
![document retrieval](/docs/arch_diagrams/search_indexer-WikiRetrieval.drawio.svg)

The document retrieval does the following steps to ensure:
* The latest version of the documents is retrieved
* if the latest version is not intended, the user can configure it to a specific dump date
* user configures how many downloads would happen simultaneously in the settings.  Wikimedia allows up to three at a time.
* all downloads are validated by their md5 sum
* downloads are going to be stored onto disk.  In the future, s3 buckets would be supported.

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

As of now, the project has two pipeline steps: a vectorizer and an NLP named entity recognition service.
They're both a reference point as to how to create a gRPC service to enhance the document.
Further implementations of gRPC services in multiple languages are planned.

##### Setting up the pipeline processor

The pipeline processor project is in `services/pipeline-processor`.  This application:
1. Take in a document from kafka.
   1. The type is `PipeDocument` which is a Google protocol buffer type, defined in `protobuf-projects/wikisearch-model/src/main/protobuf/pipes.proto`
   2. The topic is `pipe-document`.  The application subscribes to this topic.
2. Processes the document through a grpc pipeline of services registered in consul.
   1. In the `application.yml` file, you can configure the pipeline stages that would process.  These stages are all grpc services that take in the same interface, but different implementations meant to enhance a document.  Instructions on how to do this is below.
   2. When the application starts up, when a document is consumed from kafka, the processor will run the document through the configured pipelines defined in the application.yml file.
   3. The result of this file gets saved into a kafka topic called `enhanced-pipeline-document` which is also a PipeDocument type.

There are two projects that can be used to test with consul: `services/grpc-nlp-service` and `services/grpc-vectorizor-service`.
Each is standalone applications that will run on two different ports on your machine.
These services are configured in the application.yml file to automatically register for a consul server.  

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
Note: *client-configs* has not yet been implemented.
So, for now, the `client-configs` parameter can be ignored.
This is going to be refactored in the future.
The consul services would not need
to be added to the client-config section soon as the app will assume the service name is the reference to the grpc service in consul.

The above is referenced with the following instructions for the config:
1. `consul.client.registration` should be false.  Soon this will turn to true as the pipeline-processor itself will offer a grpc service to process a specific pipeline processor.  However, if this is set to true, there will be no harm as the app will still run.
2. `defaultZone` contains the consul server configuration.  Eventually, all the 
3. configuration will be in consul as an option.
4. `consul.client.discovery` needs to be true.  It's the feature of micronaut that allows for the dynamic registration of a `PipeDocument` processor.
5. `grpc.client.plaintext` is only true because the two services that come as an example are also serving as plaintext.  However, set up your ssl per the directions in micronaut grpc if you don't want to use plaintext.
6. `grpc.client.discovery.enabled` needs to be set to true to get the grpc channel from consul.
7. `kafka.enabled` is set to true.  Please read the kafka instructions if you're not using standard ports, etc. for the kafka server.
8. `pipeline-config.client-config` feature is not yet implemented.   Right now, this app assumes consul registered grpc services only.
9. `pipeline-config.pipeline-processor` sets up different named pipelines for the registered grpc consul services.  All grpc services must implement the PipeService interface for gRPC and registered in consul as a service.
10. `pipeline-config.pipeline-processor.*.services` the wildcard is the name of the defined pipeline for the `PipeDocument` processing.


## install directions

The docker directory has the latest confluent platform. It also contains a script that automatically creates all the
necessary topics for this application.

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

* learn more about kafka topics.
  About 1/2 way through some books, not jumping into any of the super fancy stuff
  yet.
* figure out if we will use key/value storage with KTable? I feel odd moving away from the avro binary to 
* json,
* get a pipeline arch going which will allow for registering multiple pipeline services using gRPC. The idea is to
  create a templated request service and the end user can create the service which is "registered" in the application yml
  of the pipeline project.

# things to cache/store outside the pipeline

* NLP cache by revision ID key to reduce the cost of processing
* create a feature that doesn't parse the articles outside the raw stage if it's already in the db and marked
  processed
* find a pluggable way to add enhancements to the document. probably just by tagging and a revision ID along with some
  more metadata. trying to avoid doing an SQL database and if I do, consider using the SQL dumps from wikimedia. But I
  want to keep this search-centric and not use that model.
* start a document store once the document is cleaned. can you use a PipeDocument topic for now?
* once we get this into solr, create https://vespa.ai/ search engine
* also consider using weaviate

# categories listed

A ton of documents cause redirects.
Start seeing how we can get that data because the wikiparser doesn't seem to give
the article id when redirecting.
It'll be cool to save that data because it's an amazing source of similarity stuff to
do with the dense vectors.

# What is the purpose of the project?

This project is made to test a large practical set of data on a search engine to practice on improving a search
experience.
As of now, we're only dealing with wikipedia data.

This is a maven project that's a self-contained executable made to install, hydrate, and create a full search engine
for wikipedia. The goal is to target java developers who are interested in learning more about advanced search features
in solr.

I saw that solr now has dense vector indices—which is a new way to store and rank documents, but has a far deeper
learning curve than the keyword matching via BM25 algorithm that solr defaults to. I'm creating this project to quickly
index wikipedia, so I can quickly test multiple dense vector configurations.

Once I get the above completed, which should take a few months, I'd like to introduce other datasets:

* weather data
* city 311 data
* income tax data
* transportation data from major cities
* census data

Open for any suggestions.

# Platform Dependent Notes


I'm building this as a linux build, but often code on OSX and Windoze.  Therefore, you can expect this software to likely work on a linux box running on an intel chipset.

Since the pytorch library is platform-dependent, there are a number of things that we had to wrangle to get this to work right between the various platforms.  Even then, I can't promise this would work natively out-of-the-box unless you're using my docker images.

## Windows Notes
When coding on Windoze, I use the WSL2 library.
I wouldn't recommend doing this to anyone at this time because the grpc services take advantage of GPUs,
and I'm not sure if the windoze machines can do that.
Regardless, it should work and run on a Windows machine.
If you can't get it to run, send me a note or open up an issue, and I'll be glad to fix it ASAP.


## Linux
I have an nvidia 2070 RTX, and I installed the following libraries on my ubuntu server:
* Docker
* JDK 17 (JDK 21 doesn't seem to work due to an incompatibility with Mockito at this time)
* maven 3.9.5
* nvidia-cuda-toolkit
* nvidia-cudnn

The build is working on the auto-build for GitHub,
but I cannot promise full compatibility without using the docker images I'm pushing to docker registry.

Protocol buffers should work OOTB without installing.  Maven handles downloading the native executable.

## OSX

Build works on OSX, but I'd recommend running on native linux instead.  It seems to be at least 50% faster on a linux machine.

That being said if you have:
* 3.9.5 maven
* JDK 17 (amazon)
* Docker Desktop

All running on OSX, I suspect this should work.

I'm attempting to put the non-CPU intensive services onto a swarm of raspberry pis in my office.  So I'll likely make both ARM64 and AMD64 docker images.  However, I'm going to start with intel images for now.


# Tools used—outside support

## WikiMedia foundation

WikiMedia makes this project go a lot easier.
They have been a great partner
to show me all about how they do their own parsing as well as just be open about how they do their own architecture internally.
Please support them!

## IntelliJ IDEA
Special shout out to JetBrains for including a free IntelliJ IDEA Ultimate license to develop this software.  

![IntelliJ IDEA Logo](/docs/intellij_logo.png)


## YourKit profiler

The people at [YourKit](https://www.yourkit.com) provided us with their profiler.
I can't say enough good things about this profiler.
It's amazing at finding errors in your code that you would never've 
 known exists.
Great for large data processing like this.

![YourKit Logo](/docs/yourkit_logo.png)
