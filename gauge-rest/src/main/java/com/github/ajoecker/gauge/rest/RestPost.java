package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.gauge.Service;
import com.thoughtworks.gauge.Step;

public class RestPost extends Service<RestConnector> {
    @Step({"When posting <query> to <path>", "And posting <query> to <path>"})
    public void posting(String query, String path) {
        connector.post(query, path, loginHandler);
    }
}
