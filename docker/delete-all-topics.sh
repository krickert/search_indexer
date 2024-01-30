#!/bin/bash
docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic download-request

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic wiki-dump-file

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic wiki-parsed-article

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic pipeline-document

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic search-document

docker exec broker \
kafka-topics --bootstrap-server broker:9092 \
             --delete \
             --topic enhanced-document

