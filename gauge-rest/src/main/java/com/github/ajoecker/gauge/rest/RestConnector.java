package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Strings;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariables;

public class RestConnector extends Connector {
    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource     the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void get(String resource, String parameter, LoginHandler loginHandler) {
        resource = replaceVariables(resource, this);
        response = get(resource, parameter, login(loginHandler));
        setPreviousResponse();
    }

    /**
     * Sends a get with the given query to the given {@link RequestSpecification}
     *
     * @param query   the query
     * @param request the request
     * @return the {@link Response}
     */
    private Response get(String query, String parameters, RequestSpecification request) {
        String queryPath = getCompleteEndpoint(query);
        if (!Strings.isNullOrEmpty(parameters)) {
            queryPath = queryPath + "?" + parameters;
        }
        return checkDebugPrint(request.contentType(ContentType.JSON)
                .when()
                .get(queryPath));
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void put(String query, String parameter, String path, LoginHandler loginHandler) {
        query = replaceVariables(query, this);
        response = put(query, parameter, path, login(loginHandler));
        setPreviousResponse();
    }

    private Response put(String query, String parameter, String path, RequestSpecification request) {
        String theEndpoint = getCompleteEndpoint(replaceVariables(path, this));
        Object object = bodyFor(query);
        return checkDebugPrint(request.contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(object)
                .when()
                .put(theEndpoint));
    }

    public void deleteWithLogin(String query, String path, LoginHandler loginHandler) {
        RequestSpecification request = login(loginHandler);
        String realQuery = replaceVariables(query, this);
        response = checkDebugPrint(request.delete(checkTrailingSlash(getCompleteEndpoint(path), realQuery)));
        setPreviousResponse();
    }

}
