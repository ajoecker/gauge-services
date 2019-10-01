package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import io.restassured.specification.RequestSpecification;

/**
 * Handles the login for a graphql query.
 */
public interface LoginHandler {
    /**
     * Sets the login information for the given request
     *
     * @param request the request that requires login
     */
    void setLogin(RequestSpecification request);

    /**
     * Logs in with the given credentials
     *
     * @param user             the user who logs in
     * @param password         the password of the user
     * @param connector the connector to send a possible login query with the credentials
     */
    void loginWithGivenCredentials(String user, String password, Connector connector);

    /**
     * Logs in with no given credentials, when no ones are required
     *
     * @param connector the connector to send a possible login query
     */
    void loginWithNoGivenCredentials(Connector connector);
}
