package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Sender;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.google.common.base.Strings;
import org.tinylog.Logger;

public class RestConnector extends Connector {
    public RestConnector(Sender sender) {
        super(sender);
    }

    public RestConnector(VariableStorage variableStorage, Sender sender) {
        super(variableStorage, sender);
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource              the query
     * @param parameter             optional parameters of the query, empty string if non available
     * @param authenticationHandler the {@link AuthenticationHandler} for authentication
     */
    void get(String resource, String parameter, AuthenticationHandler authenticationHandler) {
        String queryPath = replaceVariables(sender.getCompleteEndpoint(resource));
        if (!Strings.isNullOrEmpty(parameter)) {
            queryPath = queryPath + "?" + parameter;
        }
        Logger.info("get from {}", queryPath);
        sender.setResponse(sender.sendGet(authenticationHandler, queryPath));
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query                 the query
     * @param authenticationHandler the {@link AuthenticationHandler} for authentication
     */
    void put(String query, String path, AuthenticationHandler authenticationHandler) {
        String theEndpoint = sender.getCompleteEndpoint(replaceVariables(path));
        Logger.info("putting to {}", theEndpoint);
        Object object = bodyFor(replaceVariables(query));
        sender.setResponse(sender.sendPut(authenticationHandler, theEndpoint, object));
    }

    void deleteWithLogin(String query, String path, AuthenticationHandler authenticationHandler) {
        String base = sender.getCompleteEndpoint(replaceVariables(path));
        String deletePath = sender.checkTrailingSlash(base, replaceVariables(query));
        Logger.info("deleting from {}", deletePath);
        sender.setResponse(sender.sendDelete(authenticationHandler, deletePath));
    }
}
