package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;

import java.util.Map;

public class GraphQLSetup {
    @BeforeSuite
    public void before() {
        Registry.init(new Connector() {
            @Override
            protected Object bodyFor(String query, String variables) {
                return Map.of("query", query, "variables", variables);
            }

            @Override
            protected String withPrefix() {
                return "data.";
            }
        });
    }
}
