package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.Table;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.services.ServiceUtil.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    private String endpoint;
    private Optional<ExtractableResponse<Response>> previousResponse = Optional.empty();
    private Response response;

    public Connector() {
        setEndpoint(System.getenv("gauge.service.endpoint"));
    }

    /**
     * Returns then endpoint, the service is querying
     *
     * @return the endpoint
     */
    public final String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint, the service is querying
     *
     * @param endpoint the endpoint
     */
    public final void setEndpoint(String endpoint) {
        if (endpoint != null && !endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        this.endpoint = endpoint;
    }

    /**
     * Sends a post with the given query
     *
     * @param query the query
     */
    public final void post(String query) {
        post(query, "");
    }

    public String extract(String path)  {
        return response.then().extract().path(prefix(path));
    }

    /**
     * Sends a post with the given query and variables
     *
     * @param query     the query
     * @param variables the variables
     */
    public final void post(String query, String variables) {
        response = post(query, variables, startRequest());
        setPreviousResponse();
    }

    /**
     * Sends a get with the given query
     *
     * @param query the query
     */
    public final void get(String query) {
        response = get(query, startRequest());
        setPreviousResponse();
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public final void postWithLogin(String query, LoginHandler loginHandler) {
        response = post(query, "", login(loginHandler));
        setPreviousResponse();
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param variables    the variables
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public final void postWithLogin(String query, String variables, LoginHandler loginHandler) {
        response = post(query, variables, login(loginHandler));
        setPreviousResponse();
    }

    private void setPreviousResponse() {
        setPreviousResponse(response.then().extract());
    }

    void setPreviousResponse(ExtractableResponse<Response> previousResponse) {
        this.previousResponse = Optional.ofNullable(previousResponse);
    }

    /**
     * Returns the prefix all paths of a responses must start with.
     * <p>
     * Default is an empty string.
     *
     * @return the prefix
     */
    protected String withPrefix() {
        return "";
    }

    /**
     * Prefixes the path with the prefix ({@link #withPrefix()}) if the path does not already start with that prefix
     *
     * @param dataPath the json path
     * @return json path with guaranteed {@link #withPrefix()} at beginning
     */
    public final String prefix(String dataPath) {
        String prefix = withPrefix();
        if (prefix.length() > 0 && !dataPath.startsWith(prefix)) {
            return prefix + dataPath;
        }
        return dataPath;
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public final void getWithLogin(String query, LoginHandler loginHandler) {
        response = get(query, login(loginHandler));
        setPreviousResponse();
    }

    private RequestSpecification login(LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return request;
    }

    /**
     * Sends a post with the given query to the given {@link RequestSpecification}.
     * <p>
     * It also asserts that the status code is 200.
     *
     * @param query   the query
     * @param request the request
     * @return the {@link Response}
     */
    private Response post(String query, String variables, RequestSpecification request) {
        return checkDebugPrint(request.contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(bodyFor(query, variables))
                .when()
                .post(getEndpoint()));
    }

    private Response checkDebugPrint(Response response) {
        if (Boolean.parseBoolean(System.getenv("gauge.service.debug"))) {
            response.then().log().all();
        }
        return response;
    }

    /**
     * Allows to modify the query if the body of the request requires to format the query differently.
     * <p>
     * Default method simply returns the query and applies no changes
     *
     * @param query     the query
     * @param variables the variables, empty string if no variables available
     * @return the formatted object for the request
     */
    protected Object bodyFor(String query, String variables) {
        return query;
    }

    /**
     * Sends a get with the given query to the given {@link RequestSpecification}
     *
     * @param query   the query
     * @param request the request
     * @return the {@link Response}
     */
    private Response get(String query, RequestSpecification request) {
        return checkDebugPrint(request.contentType(ContentType.JSON)
                .when()
                .get(getEndpoint() + query));
    }

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (Boolean.parseBoolean(System.getenv("gauge.service.debug"))) {
            request.when().log().all();
        }
        return request;
    }

    public void verifyStatusCode(int expected) {
        response.then().statusCode(is(expected));
    }

    public boolean hasPreviousResponse() {
        return previousResponse.isPresent();
    }

    public Object pathFromPreviousResponse(String variablePath) {
        return previousResponse.map(pR -> pR.path(prefix(variablePath))).orElseGet(() -> "");
    }

    public void assertResponse(String path, Matcher<?> matcher) {
        response.then().assertThat().body(prefix(path), matcher);
    }

    public void clear() {
        previousResponse = Optional.empty();
    }

    public Consumer<Object[]> thenContains(String dataPath) {
        return items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, Matchers.hasItems(items));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) items[0]));
            }
        };
    }

    public Consumer<Object[]> thenIs(String dataPath) {
        return items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        };
    }
}
