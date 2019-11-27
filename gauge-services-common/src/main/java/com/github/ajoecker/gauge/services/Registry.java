package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.TokenBasedAuthentication;
import org.tinylog.Logger;

import java.util.Optional;

public final class Registry {
    public enum LoginType {
        TOKEN, BASIC
    }

    private static AuthenticationHandler authenticationHandler;
    private static Connector connector;

    private Registry() {
        // static
    }

    public static void init(Connector connector) {
        String type = Optional.ofNullable(System.getenv("gauge.service.loginhandler")).orElse(LoginType.BASIC.toString());
        Logger.info("loginhandler = " + type);
        init(connector, getLoginHandler(type));
    }

    public static void init(Connector connector, AuthenticationHandler authenticationHandler) {
        Registry.connector = connector;
        Registry.authenticationHandler = authenticationHandler;
    }

    public static Connector getConnector() {
        return connector;
    }

    public static AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    private static AuthenticationHandler getLoginHandler(String type) {
        switch (LoginType.valueOf(type.toUpperCase())) {
            case BASIC:
                return new BasicAuthentication();

            case TOKEN:
                return new TokenBasedAuthentication();

            default:
                throw new IllegalArgumentException("unknown type for login: " + type);
        }
    }
}
