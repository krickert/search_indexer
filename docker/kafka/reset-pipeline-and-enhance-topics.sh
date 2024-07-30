#!/bin/bash

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic pipeline-document

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic enhanced-document

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic pipeline-document \
             --partitions 30

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --create \
             --topic enhanced-document \
             --partitions 30

