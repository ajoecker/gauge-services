package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;
import com.thoughtworks.gauge.Step;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class GraphqlSetup {
    private static final String GRAPHQL_TYPE = "graphql";

    @BeforeSuite
    public void before() {
        Logger.info("setting up graphql service");
        Registry.get().init(GRAPHQL_TYPE, sender -> new Connector(sender, "data.") {
            @Override
            protected Object bodyFor(String query) {
                return new Yaml().<Map<String, Object>>load(query);
            }
        });
    }

    @Step("Given the graphql endpoint <endpoint>")
    public void setGraphqlEndpoint(String endpoint) {
        Registry registry = Registry.get();
        registry.sender().setEndpoint(endpoint);
        registry.setActiveType(GRAPHQL_TYPE);
    }
}
