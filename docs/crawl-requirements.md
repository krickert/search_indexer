# Crawl Database Microservice Requirements

## Overview

The system uses either OpenSearch or Solr as a crawl database to manage each individual crawl item. Each data source will operate as its own microservice and leverage a separate microservice for the crawl database. This architecture allows multiple data sources to contribute to a single index. Concurrent crawls of the same data source are not allowed to ensure data consistency. Each crawl ID is uniquely generated per data source, enabling the search engine schema to either store duplicates or merge them into the same document, thereby ensuring efficient data management and tracking.

## Crawl Item Metadata

Each crawl item should include the following identifiers and attributes:

- **Crawl ID**: Unique identifier for the crawl, generated uniquely per data source to avoid conflicts.
- **Crawl Timestamp**: Timestamp indicating when the crawl was initiated.
- **Source ID**: Unique identifier for the data source.
- **Item ID**: Unique identifier for the individual item.
- **Item URL**: URL or identifier of the item.
- **Item Status**: Status of the crawl for the item (e.g., pending, success, failure).
- **Crawl Retry Count**: Number of times the item has been retried.
- **Datasource Configuration ID**: Identifier linking the item to its data source configuration.

## Data Source Metadata

Each data source should have its own metadata, which includes:

- **Datasource ID**: Unique identifier for the data source.
- **Datasource Name**: A human-readable name for the data source.
- **Last Crawl Start Timestamp**: The timestamp when the last crawl for this data source started.
- **Live Update Tracking**: Information on how live updates are managed for the data source.
- **Deletion Capability**: Indicates if the data source is capable of explicitly marking items for deletion.
- **Microservice Configuration**: Metadata required for managing the data source as an independent microservice.

## Crawl History Metadata

Each crawl should have a history record that includes:

- **Crawl ID**: Unique identifier for the crawl.
- **Crawl Start Timestamp**: Timestamp indicating when the crawl started.
- **Crawl End Timestamp**: Timestamp indicating when the crawl ended.
- **Total Items Crawled**: The number of items that were crawled.
- **Average Item Processing Time**: The average time taken to process each item.
- **Input Speed**: The rate at which items were read from the data source.
- **Output Speed**: The rate at which items were indexed into the target database.
- **Crawl Status**: Status of the crawl (e.g., completed, failed, in-progress).
- **Microservice ID**: The identifier of the microservice responsible for the crawl.

## Crawl and Update Process

- **Kafka for Updates**: Use Kafka to capture updates for each item. Kafka will be used with auto topic generation, with reasonable configuration defaults for partitioning. The number of consumers will depend on the number of partitions. Each partition can process UUIDs independently and in order, without worrying about other partitions. Kafka will return only the most recent UUID for each item, allowing for faster reprocessing and avoiding the need to process multiple update messages (e.g., create, update, update) by focusing on the latest update.
  - **Offset Tracking**: Kafka consumer groups will be used to track offsets for reliable data streaming. Each consumer group will be responsible for a set of partitions to ensure fault tolerance and parallel processing.
  - **Partition Assignment Strategy**: Kafka will use a round-robin or range assignment strategy to distribute partitions among consumers, allowing for load balancing and efficient resource utilization.
  - **Consumer Group Configuration**: Consumers will be configured to automatically rebalance in case of failures or changes in the number of consumers, ensuring continuous processing without manual intervention.
- **Track Updates**: Use lastUpdated timestamps to track updates.
- **Orphan Handling**: Handle orphans by querying the data source at the beginning of a crawl and ensuring that only data from that specific point in time is indexed. Live updates can occur, and if there is a conflict, the live update should take precedence. IDs will be sent to a Kafka topic, and listeners will retrieve that data and process it. The updater is ignorant of the list, but if an update fails, the listener will notify the crawler. Kafka receipts will be used to ensure consistency in processing.
  - **Deletion Timing and Triggers**: Orphan deletions will be triggered after a crawl is completed and after live updates have been processed beyond the crawl start time. A scheduled job will periodically check for orphans and delete items with lastUpdated timestamps older than the crawl start time. This will ensure that stale data is removed consistently across distributed environments.
  - **Consistency Measures**: To maintain consistency in a distributed environment, Kafka receipts and acknowledgments will be used to track successful processing of updates and deletions. Each deletion operation will be atomic to prevent partial deletions or inconsistencies.
- **Replay Indexing**: Allow for replaying the indexing from the point where the crawl ended to ensure consistency. To prevent double indexing and ensure idempotency, each item will be uniquely identified using a combination of its **Crawl ID** and **Item ID**. A checksum or hash of the item data will also be used to detect changes and avoid re-indexing unchanged items.
- **Recent Document Updates**: Update only the most recent documents, leveraging Kafka's ability to track recent UUIDs.
- **Deletion Process**: Deletion should be handled based on the data source references within each document. If an item is associated with multiple data sources, only the reference to the specific data source being deleted will be removed. Full deletion of a document will only occur when no data source references remain. This ensures that duplicate data from multiple data sources is managed efficiently.
- **Error Handling and Retry Strategy**: Each data source will have a retry configuration with a retry time and a multiplier. If an item fails, it will mark the item as a failure and increment the failure count in the metadata (failure count is on a per-crawl basis). The retry strategy will include:
  - **Maximum Retries**: A configurable maximum number of retry attempts for each item.
  - **Backoff Strategy**: An exponential backoff strategy to prevent overwhelming the system with retries.
  - **Failure Escalation**: Items that consistently fail after reaching the maximum retry limit will be escalated for manual intervention or logged for further analysis.
- **Concurrency and Consistency**: To improve scalability, allow concurrent crawls while ensuring data consistency using the following mechanisms:
  - **Optimistic Locking**: Attach a version number to each item that is updated during a crawl. Before writing an update, ensure the version matches the latest, otherwise discard or retry.
  - **Conflict Resolution Strategy**: Use timestamps to determine which update is the most recent and prioritize it.
  - **UUID Partitioning**: Ensure that concurrent crawls are handled in separate Kafka partitions, and partition keys are used to guarantee that each unique item is processed independently.

## System Monitoring

- **Monitoring Framework**: Use Prometheus for monitoring key metrics and Grafana for visualization. Prometheus and Grafana are well-suited for integration with both OpenSearch and Solr, providing comprehensive monitoring capabilities.
- **Key Performance Indicators (KPIs)**:
  - **Crawl Success Rate**: Measure the percentage of successfully crawled items versus total attempted items.
  - **Crawl Latency**: Track the average time taken to process each crawl item.
  - **Error Count**: Monitor the number of errors encountered during crawls, including retries and failed items.
  - **Throughput**: Measure the input and output speeds to ensure optimal performance.
  - **System Resource Utilization**: Track CPU, memory, and disk usage for each microservice to identify bottlenecks.
- **Alerting**: Set up alerts for key metrics (e.g., high failure rates, high latency, low throughput) to proactively address issues before they impact system performance.

## Scalability Considerations

- **Horizontal and Vertical Scaling**: Consider scaling the microservices both horizontally and vertically to handle increased crawl volume. Kafka topics, consumers, and Solr/OpenSearch nodes should be designed to scale dynamically as the load increases.
  - **Kafka Topics and Partitions**: Configure Kafka topics with multiple partitions to allow parallel processing. The number of partitions should scale based on the expected crawl volume.
  - **Consumer Scaling**: Add more Kafka consumers to handle increased partition load, ensuring that the consumer group can keep up with the data ingestion rate.
  - **Solr/OpenSearch Node Scaling**: Add more Solr or OpenSearch nodes to handle increased query and indexing loads. This can be achieved using sharding and replication to distribute the data and queries across multiple nodes.
  - **Load Balancing**: Use load balancing to distribute requests across multiple microservices to prevent any single service from becoming a bottleneck.

## Gaps and Suggestions for Improvement

1. **Concurrency and Consistency**: The requirement that concurrent crawls of the same data source are not allowed could limit scalability. Consider allowing concurrent crawls with mechanisms to handle overlapping updates, such as optimistic locking or version control.

2. **Error Handling and Retry Strategy**: The retry mechanism for failed items is mentioned, but details on maximum retries, backoff strategies, or handling items that consistently fail are missing. Adding these details would improve reliability.

3. **Live Update Tracking**: The approach for managing live updates is not fully detailed. Specifically, how Kafka integrates with the system to handle live updates needs further clarification. Consider specifying how offsets, partitions, and consumers will be managed to ensure reliable data streaming.

4. **Orphan Deletion Process**: The orphan deletion process could be made more explicit. For example, the timing and triggers for deletion after a crawl are important. It would be beneficial to add more details on how to ensure consistency when deleting orphans, especially in a distributed environment.

5. **Replay Indexing Consistency**: Replay indexing is mentioned, but details on how to prevent double indexing or how to ensure idempotency are lacking. Consider specifying mechanisms to uniquely identify and track each indexed item during replay.

6. **System Monitoring**: The requirements do not specify how the crawl system will be monitored, especially for failures, latency, and throughput. Adding a monitoring and alerting framework (such as Prometheus or Elasticsearch monitoring) would be beneficial.

7. **Scalability Considerations**: It would be helpful to include considerations for scaling the microservices—both horizontally and vertically. This includes defining how Kafka topics, consumers, and Solr/OpenSearch nodes can scale to handle increased crawl volume.

8. **Data Source Deletion Capability**: The requirements mention the deletion capability for data sources but do not detail how deletions are managed if the data source does not support this capability. A fallback strategy for managing deletions in such scenarios would be useful.

