package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.github.ajoecker.gauge.services.login.TokenBasedLogin;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.orDefault;

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
        init(connector, getLoginHandler(orDefault("gauge.service.loginhandler", LoginType.BASIC.toString())));
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
