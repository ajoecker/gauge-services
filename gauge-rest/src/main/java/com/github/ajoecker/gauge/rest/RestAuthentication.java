package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;

public class RestAuthentication extends Service<RestConnector> {
    @Step({"When logging in", "And logging in"})
    public void loginWIthNoCredentials() {
        loginHandler.loginWithSystemCredentials(connector.requestSender());
    }

    @Step({"When logging in with <user> and <password>", "And logging in with <user> and <password>"})
    public void loginUserWithPassword(String user, String password) {
        loginHandler.loginWithUserPassword(user, password, connector);
    }
}
