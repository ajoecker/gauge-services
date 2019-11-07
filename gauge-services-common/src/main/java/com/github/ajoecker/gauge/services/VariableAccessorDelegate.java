package com.github.ajoecker.gauge.services;

public class VariableAccessorDelegate extends VariableAccessor {
    private VariableAccessor variableAccessor;

    public VariableAccessorDelegate(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
    }

    @Override
    public boolean logFailure() {
        return variableAccessor.logFailure();
    }

    @Override
    public boolean logAll() {
        return variableAccessor.logAll();
    }

    @Override
    public String user() {
        return variableAccessor.user();
    }

    @Override
    public String password() {
        return variableAccessor.password();
    }

    @Override
    public String token() {
        return variableAccessor.token();
    }

    @Override
    public String tokenPath() {
        return variableAccessor.tokenPath();
    }

    @Override
    public String tokenQueryFile() {
        return variableAccessor.tokenQueryFile();
    }

    @Override
    public String endpoint() {
        return variableAccessor.endpoint();
    }
}
