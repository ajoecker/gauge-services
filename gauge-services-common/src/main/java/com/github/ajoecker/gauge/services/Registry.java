package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.github.ajoecker.gauge.services.login.TokenBasedLogin;
import com.github.ajoecker.gauge.services.login.BasicAuthentication;

import java.util.Optional;

public final class Registry {
    public enum LoginType {
        TOKEN, BASIC
    }

    private static LoginHandler loginHandler;
    private static Connector connector;

    private Registry() {
        // static
    }

    public static void init(Connector connector) {
        String type = Optional.ofNullable(System.getenv("gauge.service.loginhandler")).orElse(LoginType.BASIC.toString());
        init(connector, getLoginHandler(type));
    }

    public static void init(Connector connector, LoginHandler loginHandler) {
        Registry.connector = connector;
        Registry.loginHandler = loginHandler;
    }

    public static Connector getConnector() {
        return connector;
    }

    public static LoginHandler getLoginHandler() {
        return loginHandler;
    }

    private static LoginHandler getLoginHandler(String type) {
        switch (LoginType.valueOf(type.toUpperCase())) {
            case BASIC:
                return new BasicAuthentication();

            case TOKEN:
                return new TokenBasedLogin();

            default:
                throw new IllegalArgumentException("unknown type for login: " + type);
        }
    }
}
