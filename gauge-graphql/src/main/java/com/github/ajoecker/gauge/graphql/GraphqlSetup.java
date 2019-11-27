package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class GraphqlSetup {
    @BeforeSuite
    public void before() {
        Logger.info("setting up graphql service");
        Registry.init(new Connector("data.") {
            @Override
            protected Object bodyFor(String query) {
                return new Yaml().<Map<String, Object>>load(query);
            }
        });
    }
}
