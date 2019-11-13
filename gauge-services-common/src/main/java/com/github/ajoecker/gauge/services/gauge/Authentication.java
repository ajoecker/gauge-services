package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.VariableAccessorDelegate;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link Authentication} contains all steps for user authentication.
 */
public class Authentication extends Service {
    @Step({"When <user> logs in with password <password>", "And <user> logs in with password <password>"})
    public void loginUserWithPassword(String user, String password) {
        loginHandler.loginWithGivenCredentials(user, password, connector);
    }

    @Step({"When user logs in", "And user logs in"})
    public void loginWIthNoCredentials() {
        loginHandler.loginWithSystemCredentials(connector);
    }

    @Step({"When user logs in with <token>", "And user logs in with <token>"})
    public void loginWithToken(String token) {
        VariableAccessor current = connector.getVariableAccessor();
        connector.setVariableAccessor(new VariableAccessorDelegate(current) {
            @Override
            public String token() {
                return token;
            }
        });
        loginWIthNoCredentials();
    }
}
