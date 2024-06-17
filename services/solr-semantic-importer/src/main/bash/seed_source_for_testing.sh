#!/bin/bash
docker run --name my_solr7 -d -p 8983:8983 solr:7.7.3 bash -c "solr start -f -c"
docker run --name my_solr9 -d -p 8984:8983 solr:9.6.1 bash -c "solr start -f -c"

curl -L "http://localhost:8983/solr/admin/collections?action=CREATE&name=products_source&numShards=1&replicationFactor=1&maxShardsPerNode=1&collection.configName=_default"
curl -L "http://localhost:8984/solr/admin/collections?action=CREATE&name=products_destination&numShards=1&replicationFactor=1&maxShardsPerNode=1&collection.configName=_default"

BASE_URL="http://localhost:8983/solr/products_source/update"
HEADER="Content-Type: application/json"

for i in {1..12}
do
    curl $BASE_URL -H "$HEADER" -d "
    [
        {
            \"id\": \"product_$i\",
            \"name\": \"Product $i\",
            \"price\": $i,
            \"category\": \"Category $(($i % 3 + 1))\",
            \"in_stock\": true
        }
    ]"
done

# Commit the changes
curl "$BASE_URL?commit=true"