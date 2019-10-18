package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.github.ajoecker.gauge.services.login.TokenBasedLogin;

import static com.github.ajoecker.gauge.services.ServiceUtil.orDefault;

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
        Registry.connector = connector;
        setLoginHandler(orDefault("gauge.service.loginhandler", Registry.LoginType.TOKEN.toString()));
    }

    static Connector getConnector() {
        return connector;
    }

    static LoginHandler getLoginHandler() {
        return loginHandler;
    }

    private static void setLoginHandler(String type) {
        switch (LoginType.valueOf(type.toUpperCase())) {
            case BASIC:
                loginHandler = new BasicAuthentication();
                return;

            case TOKEN:
                loginHandler = new TokenBasedLogin();
                return;

            default:
                throw new IllegalArgumentException("unknown type for login: " + type);

        }
    }
}
