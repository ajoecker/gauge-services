package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.services.ServiceUtil.*;
import static com.github.ajoecker.gauge.services.ServiceUtil.split;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;

/**
 * The class provides the implementation of the gauge specs to validate different queries.
 */
public class GaugeService {
    private final Connector connector = Registry.getConnector();
    private final LoginHandler loginHandler = Registry.getLoginHandler();

    @Step("When posting <query>")
    public void posting(String query) {
        postWithVariables(query, "");
    }

    private void postWithVariables(String query, String variables) {
        connector.postWithLogin(query, variables, loginHandler);
    }

    @Step("When getting <query>")
    public void get(String query) {
        connector.getWithLogin(query, loginHandler);
    }

    private void compare(Object value, Consumer<Object[]> match) {
        if (value instanceof String) {
            compareStringValue((String) value, match);
        } else if (value instanceof Table) {
            List<Map<String, String>> expected = ((Table) value).getTableRows().stream().map(ServiceUtil::fromTable).collect(Collectors.toList());
            match.accept(expected.toArray(new Map[expected.size()]));
        }
    }

    private void compareStringValue(String value, Consumer<Object[]> match) {
        String stringValue = value;
        if (isMap(stringValue)) {
            List<Map<String, String>> expected = parseMap(stringValue);
            match.accept(expected.toArray(new Map[expected.size()]));
        } else {
            List<String> expected = Arrays.asList(split(stringValue));
            match.accept(expected.toArray(new String[expected.size()]));
        }
    }

    @Step({"Then status code is <code>", "And status code is <code>"})
    public void verifyStatusCode(int expected) {
        connector.verifyStatusCode(expected);
    }

    @Step({"When posting <query> with <variables>", "And posting <query> with <variables>"})
    public void postingWithVariables(String query, Object variables) {
        if (variables instanceof String) {
            String variablesAsString = (String) variables;
            if (variablesAsString.trim().startsWith("{") && variablesAsString.trim().endsWith("}")) {
                postWithVariables(query, variablesAsString);
            } else {
                posting(replaceVariablesInQuery(query, variablesAsString, connector));
            }
        } else if (variables instanceof Table) {
            posting(replaceVariablesInQuery(query, (Table) variables, connector));
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }

    @Step("Given <user> logs in with password <password>")
    public void login(String user, String password) {
        loginHandler.loginWithGivenCredentials(user, password, connector);
    }

    @Step("Given user logs in")
    public void loginWIthNoCredentials() {
        loginHandler.loginWithSystemCredentials(connector);
    }

    @Step({"Then <path> contains <value>", "And <path> contains <value>"})
    public void thenContains(String dataPath, Object value) {
        compare(value, connector.thenContains(dataPath));
    }

    @Step({"Then <path> is <value>", "And <path> is <value>",
            "Then <path> are <value>", "And <path> are <value>"})
    public void thenIs(String dataPath, Object value) {
        compare(value, connector.thenIs(dataPath));
    }

    @Step("Use <endpoint>")
    public void useEndpoint(String endpoint) {
        connector.setEndpoint(endpoint);
    }


    @Step({"Then <dataPath> is empty", "And <dataPath> is empty"})
    public void thenEmpty(String dataPath) {
        connector.assertResponse(connector.prefix(dataPath), empty());
    }

    @AfterScenario
    public void clearResponse() {
        connector.clear();
    }
}
