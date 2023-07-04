# list of possible enhancements/ideas

* IP address look up
  DB - https://lite.ip2location.com/database/ip-country-region-city-latitude-longitude-zipcode-timezone
    * https://github.com/ip2location/ip2location-java
* Wikipedia related
    * click data - would be great for learn to rank
    * editing data - take IP address info and lat/log lookup? would love to see what articles have been edited "around
      me"
* search engines to integrate - good exercise to use a consumer group with kafka
    * vespi ai - search engine from yahoo with built in features we're trying to achieve with solr https://vespa.ai/
    * weaviate - another search engine that's getting a lot of attention https://weaviate.io/
    * opensearch - would probably be the easiest
* document store
    * downloaded stuff
        * ip address lat/long
        * wiki raw XML that gets processed
    * metadata that was calculated
        * NLP data (article ID/revision id key)
        * cleaned wiki article
* additional data to pipeline when we generify the document pipeline
    * weather data
    * IRS
    * 311 data
    * transportation data
* components to consider
    * generic document interface - a lot of this is wiki-specific now. Figure out a good and fast customization
      interface
    * generic conector interface - consider the kafka implementation? but feels like the same work to make a generic
      interface
    * automatic connector development - come up with generic connector types to transform documents in the pipeline?
* things not to consider
    * swapping out core components except for connectors and sinks.. anything in between is going to be bias on the
      implementation 
