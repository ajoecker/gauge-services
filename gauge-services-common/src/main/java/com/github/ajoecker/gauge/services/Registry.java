package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.github.ajoecker.gauge.services.login.TokenBasedLogin;

import static com.github.ajoecker.gauge.services.ServiceUtil.orDefault;

public final class Registry {
    public enum LoginType {
        token, basic
    }

    private static LoginHandler loginHandler;
    private static Connector connector;

    private Registry() {
        // static
    }

    public static void init(Connector connector) {
        Registry.connector = connector;
        setLoginHandler(orDefault("gauge.service.loginhandler", Registry.LoginType.token.toString()));
    }

    static Connector getConnector() {
        return connector;
    }

    static LoginHandler getLoginHandler() {
        return loginHandler;
    }

    private static void setLoginHandler(String type) {
        switch (LoginType.valueOf(type)) {
            case basic:
                loginHandler = new BasicAuthentication();
                return;

            case token:
                loginHandler = new TokenBasedLogin();
                return;

            default:
                throw new IllegalArgumentException("unknown type for login: " + type);

        }
    }
}
