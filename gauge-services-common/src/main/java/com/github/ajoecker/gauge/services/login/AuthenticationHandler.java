package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Sender;
import io.restassured.specification.RequestSpecification;

/**
 * Handles the login for a graphql query.
 */
public interface AuthenticationHandler {
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
    default void loginWithUserPassword(String user, String password, Connector connector) {
        throw new IllegalCallerException("not implemented for " + this.getClass());
    }

    /**
     * Logs in with credentials read from the gauge environment instead of given directly as in {@link #loginWithUserPassword(String, String, Connector)}
     *
     * @param connector the connector to send a possible login query
     */
    default void loginWithSystemCredentials(Sender connector) {
        throw new IllegalCallerException("not implemented for " + this.getClass());
    }

    default void loginWithQuery(String query, String tokenPath, Connector connector) {
        throw new IllegalCallerException("not implemented for " + this.getClass());
    }

    default void loginWithToken(String token) {
        throw new IllegalCallerException("not implemented for " + this.getClass());
    }
}
