package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

/**
 * A {@link AuthenticationHandler} that works based on a token.
 * <p>
 * The token can be either stated directly in the gauge environment via the configuration <code>gauge.service.token</code>
 * <p>
 * Or the token can be dynamically queried, when the configurations <code>gauge.service.token.query</code> (the file
 * with the query to login) and <code>gauge.service.token.path</code> (the jsonpath to the token in the response) are given.
 */
public final class TokenBasedAuthentication implements AuthenticationHandler {
    private String loginToken;

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(loginToken)) {
            request.auth().preemptive().oauth2(loginToken);
        }
    }

    @Override
    public void loginWithToken(String token) {
        this.loginToken = token;
    }

    @Override
    public void loginWithQuery(String query, String tokenPath, Connector connector) {
        connector.post(query);
        loginToken = connector.fromLatestResponse(tokenPath).toString();
    }
}
