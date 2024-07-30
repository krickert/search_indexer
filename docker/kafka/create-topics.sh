#!/bin/bash
docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic download-request \
             --partitions 1 

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic wiki-dump-file \
             --partitions 4

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic wiki-parsed-article \
             --partitions 30

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic pipeline-document \
             --partitions 30

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic search-document \
             --partitions 30

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic enhanced-document \
             --partitions 30
