package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import java.util.stream.Collectors;

public class Rest extends Service<RestConnector> {
    @Step({"When deleting <query>", "And deleting <query>"})
    public void delete(String query) {
        connector().deleteWithLogin(query, "", authenticationHandler);
    }

    @Step({"When deleting <query> from <path>", "And deleting <query> from <path>"})
    public void delete(String query, String path) {
        connector().deleteWithLogin(query, path, authenticationHandler);
    }

    @Step({"When getting <resource>", "And getting <resource>"})
    public void get(String resource) {
        connector().get(resource, "", authenticationHandler);
    }

    @Step({"When getting <query> with <parameters>", "And getting <query> with <parameters>"})
    public void gettingParameters(String query, Object parameters) {
        if (parameters instanceof Table) {
            getForGaugeTable(query, (Table) parameters);
        } else if (parameters instanceof String) {
            // the string is already in the format var1=val,var2=val2 - so only , must be replaced with &
            connector().get(query, ((String) parameters).replaceAll("\\s+", "").replace(',', '&'), authenticationHandler);
        }
    }

    // transforms the gauge table in a set of parameters like var1=val1&var2=val2
    private void getForGaugeTable(String query, Table table) {
        connector().get(query, toParameter(table), authenticationHandler);
    }

    private String toParameter(Table table) {
        return table.getTableRows().stream()
                    .map(tableRow -> tableRow.getCell("name") + "=" + tableRow.getCell("value"))
                    .collect(Collectors.joining("&"));
    }

    @Step({"When putting <query>", "And putting <query>"})
    public void putting(String query) {
        connector().put(query, "", authenticationHandler);
    }

    @Step({"When putting <query> to <path> with <table>", "And putting <query> to <path> with <table>"})
    public void putting(String query, String path, Table table) {
        connector().put(query, path, table, authenticationHandler);
    }

    @Step({"When putting <query> to <path>", "And putting <query> to <path>"})
    public void putting(String query, String path) {
        connector().put(query, path, authenticationHandler);
    }

    @Step({"When logging in", "And logging in"})
    public void loginWIthNoCredentials() {
        authenticationHandler.loginWithSystemCredentials(sender);
    }

    @Step({"When logging in with <user> and <password>", "And logging in with <user> and <password>"})
    public void loginUserWithPassword(String user, String password) {
        authenticationHandler.loginWithUserPassword(user, password, connector());
    }

    @Step({"When posting <query> to <path>", "And posting <query> to <path>"})
    public void posting(String query, String path) {
        connector().post(query, path, authenticationHandler);
    }
}
