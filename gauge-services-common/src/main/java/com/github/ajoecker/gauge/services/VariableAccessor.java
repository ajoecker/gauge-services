package com.github.ajoecker.gauge.services;

public class VariableAccessor {
    public String user() {
        return System.getenv("gauge.service.user");
    }

    public String password() {
        return System.getenv("gauge.service.password");
    }

    public String token() {
        return System.getenv("gauge.service.token");
    }

    public String tokenPath() {
        return System.getenv("gauge.service.token.path");
    }

    public String tokenQueryFile() {
        return System.getenv("gauge.service.token.query");
    }
}
