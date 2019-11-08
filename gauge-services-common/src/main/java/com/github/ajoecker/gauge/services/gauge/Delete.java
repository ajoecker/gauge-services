package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;

public class Delete extends Service {
    @Step({"When deleting <query>", "And deleting <query>"})
    public void delete(String query) {
        connector.deleteWithLogin(query, "", loginHandler);
    }

    @Step({"When deleting <query> from <path>", "And deleting <query> from <path>"})
    public void delete(String query, String path) {
        connector.deleteWithLogin(query, path, loginHandler);
    }
}
