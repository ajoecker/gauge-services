package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.common.Sender;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.google.common.base.Strings;

public class RestConnector extends Connector {
    public RestConnector() {
        super();
    }

    public RestConnector(VariableStorage variableStorage, Sender sender) {
        super(variableStorage, sender);
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource     the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param authenticationHandler the {@link AuthenticationHandler} for authentication
     */
    void get(String resource, String parameter, AuthenticationHandler authenticationHandler) {
        String queryPath = replaceVariables(theSender.getCompleteEndpoint(resource));
        if (!Strings.isNullOrEmpty(parameter)) {
            queryPath = queryPath + "?" + parameter;
        }
        setResponse(theSender.sendGet(authenticationHandler, queryPath));
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param authenticationHandler the {@link AuthenticationHandler} for authentication
     */
    void put(String query, String path, AuthenticationHandler authenticationHandler) {
        String theEndpoint = theSender.getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        setResponse(theSender.sendPut(authenticationHandler, theEndpoint, object));
    }

    void deleteWithLogin(String query, String path, AuthenticationHandler authenticationHandler) {
        String base = theSender.getCompleteEndpoint(replaceVariables(path));
        String deletePath = theSender.checkTrailingSlash(base, replaceVariables(query));
        setResponse(theSender.sendDelete(authenticationHandler, deletePath));
    }
}
