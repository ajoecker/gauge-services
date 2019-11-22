package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link PUT} contains all steps for a POST request
 */
public class PUT extends Service<RestConnector> {
    @Step({"When putting <query>", "And putting <query>"})
    public void putting(String query) {
        connector.put(query, "", loginHandler);
    }

    @Step({"When putting <query> to <path>", "And putting <query> to <path>"})
    public void putting(String query, String path) {
        connector.put(query, path, loginHandler);
    }
}
