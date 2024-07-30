# README for docker compose files
This section goes over the docker-compose examples in case you want to develop locally.

The following services are dependent on this architecture:
1. [Consul service discovery](https://www.hashicorp.com/blog/explore-hashicorp-consul-with-docker-compose) - for automatic service discovery
2. [Solr Search Engine](https://solr.apache.org/guide/solr/latest/deployment-guide/solr-in-docker.html) - for semantic search
3. [Kafka and a schema registry](https://codingharbour.com/apache-kafka/how-to-use-protobuf-with-apache-kafka-and-schema-registry/) - pipelines are done via kafka and adhere to a protocol buffer convention for serialization

In theory, you can find a way to run these services without these dependencies, but it saves a lot of time if you run these services first.

We're working on a "golden compose" file that would launch all the services in one swoop.  For now, the [integration tests subproject](https://github.com/krickert/search_indexer/tree/master/integration-tests) is being developed for a single place to have an end-to-end test so junit can be used to measure the output.

## Colsul service discovery
This application uses colsul gRPC services to register pipeline services.  This means that if you'd like to have a step added to this pipeline, you can create a gRPC service using the [Pipeline service definition](https://github.com/krickert/search_indexer/blob/master/protobuf-projects/wikisearch-model/src/main/protobuf/pipeline_service.proto) then you can inject your service into the pipeline with 0 java code.

## Solr search engine
This project utilizes the Solr Search Engine, although it would take about a day to use a different search engine such as pinecone, opensearch, or even postgres.  You just need a vector store capable of performing searches.  There is a query gRPC interface should you want to write your own query.


