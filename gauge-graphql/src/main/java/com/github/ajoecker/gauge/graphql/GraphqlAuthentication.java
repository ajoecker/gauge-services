package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;

public class GraphqlAuthentication extends Service<Connector> {
    @Step({"When logging in with query <query> for <tokenPath>",
            "And logging in with query <query> for <tokenPath>"})
    public void loginWithQuery(String query, String tokenPath) {
        authenticationHandler.loginWithQuery(query, tokenPath, connector);
    }
}
