# Getting started
Right now we're working on a docker compose file to do a single run.  This would be independent of the project since the containers are located on docker.io

Once this is run, there is a script that will trigger the end-to-end which should result in a solr collection with semantic search and two client endpoints:

1. REST endpoint for text search 
2. gRPC endpoint for text search

What this proeject does:
1) Downloads the wikipedia dumps
2) Parses them and cleans up the test
3) add sentence embeddings to the articles
4) put them in solr search


The data format between services is google protocol buffers.  These requests can be done by either an async kafka topic or through a grpc request.

The point of this project is to have a fully open source solution to download wikipedia and tweak pipeline steps to see how it affects the results of the search engine.


# Developer Notes
## useful commands
### deploy docker containers
`./mvnw -Dpackaging=docker deploy`
### Start micronaut test housing
If you're in intellij, and the unit tests are not starting right - run this
`./mvnw mn:start-testresources-service`

I'm only putting this in here because whenever I try to run micronaut tests in my jetbrains IDE, it doesn't seem to start right.  Running this makes my tests runnable in IntelliJ
=
## Dependent services
The following open source projects are needed for this to work end-to-end:
* Solr 9.6
* Kafka with schema registry
* Consul

The internal functional tests automatically use the above services to properly run.

## Wikipedia downloading
Wikimedia will only allow up to 3 concurrent connections when downloading the dump files.

For now, if a download fails, it will send the request to a failure topic.


