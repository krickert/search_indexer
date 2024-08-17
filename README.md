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

h1. So why a Wiki RAG?

Please note that this project is not part of the wikimedia foundation, but they've been great at helping me get this framework to where it is now through their monthly IRC channel help.

I've been working on search technologies for 15 years, and created this project to release code patterns I've seen in the industry that aren't easily done when testing new search technologies.

The biggest problem I've seen with search is indexing—the search portion is about 10% of a project.
Wronging data into the engine is where most of the efforts are drawn.

I started this to test semantic search on wikipedia data, but it's since evolved to be a full-fledged indexing solution.

As of now, this project allows you to index all of wikipedia [directly from the wiki foundation's dump files](https://dumps.wikimedia.org/enwiki/).  It also contains a ton of standalone tools made to assist in a search engine pipeline process.

## Overview
This project is made to be an OOTB enterprise data pipeline for processing data from multiple 
sources and to be placed into a datastream for processing.

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
