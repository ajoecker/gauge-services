package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.LoginHandler;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    private String endpoint;

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
     * @return the {@link Response}
     */
    public final Response post(String query) {
        return post(query, "");
    }

    /**
     * Sends a post with the given query and variables
     *
     * @param query     the query
     * @param variables the variables
     * @return the {@link Response}
     */
    public final Response post(String query, String variables) {
        return post(query, variables, startRequest());
    }

    /**
     * Sends a get with the given query
     *
     * @param query the query
     * @return the {@link Response}
     */
    public final Response get(String query) {
        return get(query, startRequest());
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param loginHandler the {@link LoginHandler} for authentication
     * @return the {@link Response}
     */
    public final Response postWithLogin(String query, LoginHandler loginHandler) {
        return post(query, "", login(loginHandler));
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param variables    the variables
     * @param loginHandler the {@link LoginHandler} for authentication
     * @return the {@link Response}
     */
    public final Response postWithLogin(String query, String variables, LoginHandler loginHandler) {
        return post(query, variables, login(loginHandler));
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
     * @return the {@link Response}
     */
    public final Response getWithLogin(String query, LoginHandler loginHandler) {
        return get(query, login(loginHandler));
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
        if (Boolean.valueOf(System.getenv("gauge.service.debug"))) {
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
        if (Boolean.valueOf(System.getenv("gauge.service.debug"))) {
            request.when().log().all();
        }
        return request;
    }
}
