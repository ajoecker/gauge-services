package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link POST} contains all steps for a POST request
 */
public class POST extends PostService<Connector> {
    @Step({"When posting <query>", "And posting <query>"})
    public void posting(String query) {
        connector.post(query, "", loginHandler);
    }

    @Step({"When posting <query> with <parameters>", "And posting <query> with <parameters>"})
    public void postingWithParameters(String query, Object parameters) {
        postingWithVariables(query, "", parameters);
    }
}
