package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.common.Sender;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

/**
 * {@link AuthenticationHandler} that works on basic authentication.
 * <p>
 * It retrieves user and password information from the environment variables
 * <code>gauge.service.user</code> and <code>gauge.service.password</code>.
 */
public class BasicAuthentication implements AuthenticationHandler {
    private String token;
    private String user;
    private String password;

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(password)) {
            request.auth().preemptive().basic(user, password);
        }
        else if (!Strings.isNullOrEmpty(token)) {
            request.header("Authorization", "Basic " + token);
        }
    }

    @Override
    public void loginWithUserPassword(String user, String password, Connector connector) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void loginWithToken(String token) {
        this.token = token;
    }

    @Override
    public void loginWithSystemCredentials(Sender connector) {
        VariableAccessor variableAccessor = connector.getVariableAccessor();
        this.user = variableAccessor.user();
        this.password = variableAccessor.password();
        this.token = variableAccessor.token();
    }
}
