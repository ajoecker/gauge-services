package com.github.ajoecker.gauge.services;

public class VariableAccessor {
    private static final String FAILURE = "failure";
    private static final String ALL = "all";

    public boolean logFailure() {
        return checkLog(FAILURE);
    }

    private boolean checkLog(String level) {
        return level.equalsIgnoreCase(System.getenv("gauge.service.log"));
    }

    public boolean logAll() {
        return checkLog(ALL);
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
