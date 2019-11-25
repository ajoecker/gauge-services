package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link POST} contains all steps for a POST request
 */
public final class POST extends Service<Connector> {
    @Step({"When posting <query>", "And posting <query>"})
    public void posting(String query) {
        connector.post(query, "", authenticationHandler);
    }
}
