#!/bin/bash

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