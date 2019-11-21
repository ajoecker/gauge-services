package com.github.ajoecker.gauge.graphql;

import com.github.ajoecker.gauge.services.gauge.Service;
import com.thoughtworks.gauge.Step;

public class GraphqlAuthentication extends Service {
    @Step({"When logging in with query <query> and <variables> for <tokenPath>",
            "And logging in with query <query> and <variables> for <tokenPath>"})
    public void loginWithQuery(String query, String variables, String tokenPath) {
        loginHandler.loginWithQuery(query, variables, tokenPath, connector);
    }
}
