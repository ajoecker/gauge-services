package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import org.assertj.core.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.services.ServiceUtil.*;
import static com.thoughtworks.gauge.datastore.DataStoreFactory.getScenarioDataStore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;

/**
 * The class provides the implementation of the gauge specs to validate different queries.
 */
public class GaugeService {
    private final Connector connector;
    private final LoginHandler loginHandler;

    public GaugeService() {
        this(Registry.getConnector(), Registry.getLoginHandler());
    }

    public GaugeService(Connector connector, LoginHandler loginHandler) {
        this.connector = connector;
        this.loginHandler = loginHandler;
    }

    @Step("When posting <query>")
    public void posting(String query) {
        postWithVariables(query, "");
    }

    private void postWithVariables(String query, String variables) {
        connector.postWithLogin(query, variables, loginHandler);
    }

    @Step("When getting <query>")
    public void get(String query) {
        connector.getWithLogin(query, "", loginHandler);
    }

    @Step({"Then extracting <variable> from <parent> where <attribute> is <value>", "And extracting <path> from <parent> where <attribute> is <value>"})
    public void extractPathWithParent(String variable, String parent, String attributePath, String valueToMatch) {
        connector.extract(variable, parent, attributePath, valueToMatch);
    }

    @Step({"Then extracting <variable> where <attribute> is <value>", "And extracting <path> where <attribute> is <value>"})
    public void extractPath(String variable, String attributePath, String valueToMatch) {
        connector.extract(variable, "", attributePath, valueToMatch);
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

    @Step({"Then the request finished in less than <timeout> ms", "And the request finished in less than <timeout> ms",
            "Then the request finished in less than <timeout>ms", "And the request finished in less than <timeout>ms"})
    public void requestInLessThanMs(long timeout) {
        connector.verifyRequestInLessThan(timeout);
    }

    @Step({"Then the request finished in less than <timeout> s", "And the request finished in less than <timeout> s",
            "Then the request finished in less than <timeout>s", "And the request finished in less than <timeout>s"})
    public void requestInLessThan(long timeout) {
        requestInLessThanMs(timeout * 1000);
    }

    @Step({"Then status code is <code>", "And status code is <code>"})
    public void verifyStatusCode(int expected) {
        connector.verifyStatusCode(expected);
    }

    @Step({"When getting <query> with <variables>", "And getting <query> with <variables>"})
    public void gettingWithVariables(String query, Object parameters) {
        if (parameters instanceof Table) {
            Table table = (Table) parameters;
            String getParameters = table.getTableRows().stream()
                    .map(tableRow -> tableRow.getCell("name") + "=" + tableRow.getCell("value"))
                    .collect(Collectors.joining("&"));
            getWithParameters(query, getParameters);
        }
        else if(parameters instanceof String) {
            getWithParameters(query, ((String) parameters).replaceAll("\\s+", "").replace(',','&'));
        }
    }

    private void getWithParameters(String query, String variables) {
        connector.getWithLogin(query, variables, loginHandler);
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
        Object extractedCacheValue = getScenarioDataStore().get(dataPath);
        if (extractedCacheValue != null) {
            assertThat(extractedCacheValue.toString()).isEqualTo(value);
        } else {
            compare(value, connector.thenIs(dataPath));
        }
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
