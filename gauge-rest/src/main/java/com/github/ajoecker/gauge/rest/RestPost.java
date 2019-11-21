package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.gauge.PostService;
import com.github.ajoecker.gauge.services.gauge.Service;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariables;
import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariablesInQuery;

public class RestPost extends PostService<RestConnector> {
    @Step({"When posting <query> to <path>", "And posting <query> to <path>"})
    public void posting(String query, String path) {
        connector.post(query, path, loginHandler);
    }

    @Step({"When posting <query> to <path> with <variables>", "And posting <query> to <path> with <variables>"})
    public void postingWithVariables(String query, String path, Object variables) {
        super.postingWithVariables(query, path, variables);
    }
}
