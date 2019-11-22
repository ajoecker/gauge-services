package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link Delete} contains all step implementations for deleting a resource
 */
public class Delete extends Service<RestConnector> {
    @Step({"When deleting <query>", "And deleting <query>"})
    public void delete(String query) {
        connector.deleteWithLogin(query, "", loginHandler);
    }

    @Step({"When deleting <query> from <path>", "And deleting <query> from <path>"})
    public void delete(String query, String path) {
        connector.deleteWithLogin(query, path, loginHandler);
    }
}
