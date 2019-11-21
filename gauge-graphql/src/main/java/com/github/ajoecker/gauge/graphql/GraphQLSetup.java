package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class GraphQLSetup {
    @BeforeSuite
    public void before() {
        Registry.init(new Connector() {
            @Override
            protected Object bodyFor(String query) {
                return new Yaml().<Map<String, Object>>load(query);
            }

            @Override
            public String prefix(String dataPath) {
                return super.prefix("data." + dataPath);
            }
        });
    }
}
