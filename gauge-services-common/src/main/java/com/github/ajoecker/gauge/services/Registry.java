package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.TokenBasedAuthentication;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class Registry {
    private static final Registry instance = new Registry();

    public enum LoginType {
        TOKEN, BASIC
    }

    private AuthenticationHandler authenticationHandler;
    private Map<String, Connector> connectors = new HashMap<>();
    private Sender sender = new Sender(new VariableAccessor());
    private String activeType = "";

    private Registry() {
        // static
    }

    public static Registry get() {
        return instance;
    }

    public void init(String type, Function<Sender, Connector> function) {
        String loginHandler = Optional.ofNullable(System.getenv("gauge.service.loginhandler")).orElse(LoginType.BASIC.toString());
        Logger.info("loginhandler = " + loginHandler);
        init(type, sender, function.apply(sender), getLoginHandler(loginHandler));
    }

    // test-friendly
    public void init(String type, Sender sender, Connector connector, AuthenticationHandler authenticationHandler) {
        Logger.info("register connector for " + type);
        this.authenticationHandler = authenticationHandler;
        this.activeType = type;
        this.sender = sender;
        connectors.put(type, connector);
    }

    public void setActiveType(String type) {
        Logger.info("setting active type to {}", type);
        this.activeType = type;
    }

    public Connector connector() {
        return connectors.get(activeType);
    }

    public Sender sender() {
        return sender;
    }

    public AuthenticationHandler getAuthenticationHandler() {
        return authenticationHandler;
    }

    private AuthenticationHandler getLoginHandler(String type) {
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
