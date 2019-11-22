package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.common.RequestSender;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Strings;

public class RestConnector extends Connector {
    public RestConnector() {
        super();
    }

    public RestConnector(VariableStorage variableStorage, RequestSender requestSender) {
        super(variableStorage, requestSender);
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource     the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    void get(String resource, String parameter, LoginHandler loginHandler) {
        String queryPath = replaceVariables(requestSender.getCompleteEndpoint(resource));
        if (!Strings.isNullOrEmpty(parameter)) {
            queryPath = queryPath + "?" + parameter;
        }
        setResponse(requestSender.sendGet(loginHandler, queryPath));
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    void put(String query, String path, LoginHandler loginHandler) {
        String theEndpoint = requestSender.getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        setResponse(requestSender.sendPut(loginHandler, theEndpoint, object));
    }

    void deleteWithLogin(String query, String path, LoginHandler loginHandler) {
        String base = requestSender.getCompleteEndpoint(replaceVariables(path));
        String deletePath = requestSender.checkTrailingSlash(base, replaceVariables(query));
        setResponse(requestSender.sendDelete(loginHandler, deletePath));
    }
}
