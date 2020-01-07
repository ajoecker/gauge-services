package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import org.tinylog.Logger;

/**
 * The class {@link Common} contains common step implementations for all different kinds, like checking the status code,
 * the response time etc.
 */
public final class Common extends Service<Connector> {
    @Step({"Then the request finished in less than <timeout> ms", "And the request finished in less than <timeout> ms",
            "Then the request finished in less than <timeout>ms", "And the request finished in less than <timeout>ms"})
    public void requestInLessThanMs(long timeout) {
        sender.verifyRequestInLessThan(timeout);
    }

    @Step({"Then the request finished in less than <timeout> s", "And the request finished in less than <timeout> s",
            "Then the request finished in less than <timeout>s", "And the request finished in less than <timeout>s"})
    public void requestInLessThan(long timeout) {
        requestInLessThanMs(timeout * 1000);
    }

    @Step({"Then status code is <code>", "And status code is <code>"})
    public void verifyStatusCode(int expected) {
        sender.verifyStatusCode(expected);
    }

    @Step({"When waiting for <seconds> s", "And waiting for <seconds> s"})
    public void sleep(int time) {
        try {
            Thread.sleep(time * 1000l);
        } catch (InterruptedException e) {
            // that is fine
            Thread.currentThread().interrupt();
        }
    }

    @Step("Given the endpoint <endpoint>")
    public void useEndpoint(String endpoint) {
        sender.setEndpoint(endpoint);
    }

    @Step({"When logging in with token <token>", "And logging in with token <token>"})
    public void loginWithToken(String token) {
        authenticationHandler.loginWithToken(token);
    }

    @Step({"Then extracting <variable> from <parent>",
            "And extracting <variable> from <parent>"})
    public void extractPathWithParent(String variable, String parent) {
        connector().extract(variable, parent, "", variable);
    }

    @Step({"Then extracting <variable> from <parent> as <store>",
            "And extracting <variable> from <parent> as <store>"})
    public void extractPathWithParentInto(String variable, String parent, String saver) {
        connector().extract(variable, parent, "", saver);
    }

    @Step({"Then extracting <variable>", "And extracting <variable>"})
    public void extractPath(String variable) {
        connector().extract(variable, "", "", variable);
    }

    @Step({"Then extracting <variable> from <parent> where <attribute>",
            "And extracting <variable> from <parent> where <attribute>"})
    public void extractPathWithParent(String variable, String parent, String attributeValue) {
        connector().extract(variable, parent, attributeValue, variable);
    }

    @Step({"Then extracting <variable> where <attribute>", "And extracting <variable> where <attribute>"})
    public void extractPath(String variable, String attributeValue) {
        connector().extract(variable, "", attributeValue, variable);
    }

    @Step({"Then extracting <variable> as sum of <sum>", "And extracting <variable> as sum of <sum>"})
    public void extractAsSum(String variable, String sum) {
        connector().extractSum(variable, sum);
    }

    @Step({"Then extracting <pathInJson> as <variableToStore> from json <pathToJson>",
            "And extracting <pathInJson> as <variableToStore> from json <pathToJson>"})
    public void extractFromJson(String pathInJson, String variableToStore, String pathToJson) {
        connector().extractFromJson(pathInJson, pathToJson, variableToStore);
    }

    @Step({"When posting <query>", "And posting <query>"})
    public void posting(String query) {
        connector().post(query, "", authenticationHandler);
    }

    @Step({"When posting <query> with <table>", "And posting <query> with <table>"})
    public void postingWithParameters(String query, Table table) {
        connector().post(query, "", table, authenticationHandler);
    }

    @Step({"When posting from <variable>", "And posting from <variable>"})
    public void postingFromVariable(String variable) {
        Logger.info("posting query read from {}", variable);
        String query = (String) VariableStorage.get().get(variable).orElseThrow();
        connector().post(query, "", authenticationHandler);
    }
}
