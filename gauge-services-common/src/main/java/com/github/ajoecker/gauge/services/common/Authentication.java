package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link Authentication} contains all steps for user authentication.
 */
public class Authentication extends Service<Connector> {
    @Step({"When logging in with token <token>", "And logging in with token <token>"})
    public void loginWithToken(String token) {
        loginHandler.loginWithToken(token);
    }
}
