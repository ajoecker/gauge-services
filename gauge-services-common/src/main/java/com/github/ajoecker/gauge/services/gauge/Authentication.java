package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.VariableAccessorDelegate;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link Authentication} contains all steps for user authentication.
 */
public class Authentication extends Service {
    @Step({"When logging in", "And logging in"})
    public void loginWIthNoCredentials() {
        loginHandler.loginWithSystemCredentials(connector);
    }

    @Step({"When logging in with token <token>", "And logging in with token <token>"})
    public void loginWithToken(String token) {
        loginHandler.loginWithToken(token);
    }
}
