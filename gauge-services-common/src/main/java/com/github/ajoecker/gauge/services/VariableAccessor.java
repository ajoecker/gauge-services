package com.github.ajoecker.gauge.services;

public class VariableAccessor {
    private static final String FAILURE = "failure";
    private static final String ALL = "all";

    public boolean logFailure() {
        return System.getenv("gauge.service.log").equalsIgnoreCase(FAILURE);
    }

    public boolean logAll() {
        return System.getenv("gauge.service.log").equalsIgnoreCase(ALL);
    }

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

    public String endpoint() {
        return System.getenv("gauge.service.endpoint");
    }
}
