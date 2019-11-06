package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

/**
 * {@link LoginHandler} that works on basic authentication.
 * <p>
 * It retrieves user and password information from the environment variables
 * <code>gauge.service.user</code> and <code>gauge.service.password</code>.
 */
public class BasicAuthentication implements LoginHandler {
    private String user;
    private String password;
    private String token;
    private VariableAccessor variableAccessor;

    public BasicAuthentication(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
    }

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(user) && !Strings.isNullOrEmpty(password)) {
            request.auth().preemptive().basic(user, password);
        }
        else if (!Strings.isNullOrEmpty(token)) {
            request.header("Authorization", token);
        }
    }

    @Override
    public void loginWithGivenCredentials(String user, String password, Connector connector) {
        this.user = user;
        this.password = password;
    }

    @Override
    public void loginWithSystemCredentials(Connector connector) {
        this.user = variableAccessor.user();
        this.password = variableAccessor.password();
        this.token = variableAccessor.token();
    }
}
