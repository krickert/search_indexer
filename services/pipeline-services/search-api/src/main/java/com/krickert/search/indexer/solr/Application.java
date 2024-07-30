package com.krickert.search.indexer.solr;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

@OpenAPIDefinition(
        info = @Info(
                title = "Wiki Semantic Search API",
                version = "1.0-SNAPSHOT",
                description = "Semantic Search",
                license = @License(name = "GNU Public License 3.0", url = "https://github.com/krickert/search_indexer/blob/master/LICENSE.md"),
                contact = @Contact(url = "https://github.com/krickert/search_indexer/", name = "Kristian Rickert", email = "krickert.search@gmail.com")
        )
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}