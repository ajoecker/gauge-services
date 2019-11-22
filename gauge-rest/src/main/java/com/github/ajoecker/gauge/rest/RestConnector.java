package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Strings;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class RestConnector extends Connector {
    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource     the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    void get(String resource, String parameter, LoginHandler loginHandler) {
        String queryPath = replaceVariables(getCompleteEndpoint(resource));
        if (!Strings.isNullOrEmpty(parameter)) {
            queryPath = queryPath + "?" + parameter;
        }
        response = checkDebugPrint(login(loginHandler)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .get(queryPath));
        setPreviousResponse();
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    void put(String query, String path, LoginHandler loginHandler) {
        String theEndpoint = getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        response = checkDebugPrint(login(loginHandler)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(object)
                .when()
                .put(theEndpoint));
        setPreviousResponse();
    }

    void deleteWithLogin(String query, String path, LoginHandler loginHandler) {
        String deletePath = checkTrailingSlash(getCompleteEndpoint(path), replaceVariables(query));
        response = checkDebugPrint(login(loginHandler).delete(deletePath));
        setPreviousResponse();
    }
}
