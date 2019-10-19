package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.services.ServiceUtil.*;
import static org.hamcrest.Matchers.*;

/**
 * The class provides the implementation of the gauge specs to validate different queries.
 */
public class GaugeService {
    private Response response;
    private final LoginHandler loginHandler = Registry.getLoginHandler();
    private final Connector connector = Registry.getConnector();
    private Optional<ExtractableResponse<Response>> previousResponse = Optional.empty();

    @Step("When posting <query>")
    public void posting(String query) {
        postWithVariables(query, "");
    }

    private void postWithVariables(String query, String variables) {
        response = connector.postWithLogin(query, variables, loginHandler);
        previousResponse = Optional.of(response.then().extract());
    }

    @Step("When getting <query>")
    public void get(String query) {
        response = connector.getWithLogin(query, loginHandler);
        previousResponse = Optional.of(response.then().extract());
    }

    @Step({"Then status code is <code>", "And status code is <code>"})
    public void verifyStatusCode(int expected) {
        response.then().statusCode(is(expected));
    }

    @Step({"When posting <query> with <variables>", "And posting <query> with <variables>"})
    public void postingWithVariables(String query, Object variables) {
        if (variables instanceof String) {
            String variablesAsString = (String) variables;
            if (variablesAsString.trim().startsWith("{") && variablesAsString.trim().endsWith("}")) {
                postWithVariables(query, variablesAsString);
            } else {
                posting(replaceVariablesInQuery(query, variablesAsString, previousResponse, connector));
            }
        } else if (variables instanceof Table) {
            posting(replaceVariablesInQuery(query, (Table) variables, previousResponse, connector));
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
    public void thenMustContains(String dataPath, Object value) {
        compare(value, items -> {
            if (response.then().extract().path(connector.prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, hasItems(items));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) items[0]));
            }
        });
    }

    @Step({"Then <path> is <value>", "And <path> is <value>",
            "Then <path> are <value>", "And <path> are <value>"})
    public void thenMustBe(String dataPath, Object value) {
        compare(value, items -> {
            if (response.then().extract().path(connector.prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        });
    }

    @Step("Use <endpoint>")
    public void useEndpoint(String endpoint) {
        connector.setEndpoint(endpoint);
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

    @Step({"Then <dataPath> is empty", "And <dataPath> is empty"})
    public void thenEmpty(String dataPath) {
        assertResponse(connector.prefix(dataPath), empty());
    }

    private void assertResponse(String path, Matcher<?> matcher) {
        response.then().assertThat().body(connector.prefix(path), matcher);
    }

    @AfterScenario
    public void clearResponse() {
        previousResponse = Optional.empty();
    }
}
