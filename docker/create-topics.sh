#!/bin/bash
docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic download-request \
             --partitions 3 

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic wiki-dump-file \
             --partitions 8

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic wiki-parsed-article \
             --partitions 10

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic pipeline-document \
             --partitions 10

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic search-document \
             --partitions 10
