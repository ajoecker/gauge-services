package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.google.common.base.Strings;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ajoecker.gauge.services.ServiceUtil.replaceVariablesInQuery;
import static com.github.ajoecker.gauge.services.ServiceUtil.separator;
import static java.nio.file.Files.readString;

/**
 * A {@link LoginHandler} that works based on a token.
 * <p>
 * The token can be either stated directly in the gauge environment via the configuration <code>gauge.service.token</code>
 * <p>
 * Or the token can be dynamically queried, when the configurations <code>gauge.service.token.query</code> (the file
 * with the query to login) and <code>gauge.service.token.path</code> (the jsonpath to the token in the response) are given.
 */
public final class TokenBasedLogin implements LoginHandler {
    private String loginToken;

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(loginToken)) {
            request.auth().oauth2(loginToken);
        }
    }

    @Override
    public void loginWithNoGivenCredentials(Connector connector) {
        loginToken = Optional.ofNullable(System.getenv("gauge.service.token")).orElse(sendLoginQuery(connector, Function.identity()));
    }

    @Override
    public void loginWithGivenCredentials(String user, String password, Connector connector) {
        loginToken = sendLoginQuery(connector, s -> replaceVariablesInQuery(s, "user:" + user + separator() + "password:" + password, Optional.empty(), connector));
    }

    private String sendLoginQuery(Connector connector, Function<String, String> queryMapper) {
        try {
            Response sending = connector.post(readQuery(queryMapper));
            return sending.then().extract().path(System.getenv("gauge.service.token.path"));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readQuery(Function<String, String> mapper) throws IOException, URISyntaxException {
        String queryFile = "/" + System.getenv("gauge.service.token.query");
        URI uri = TokenBasedLogin.class.getResource(queryFile).toURI();
        return mapper.apply(readString(Paths.get(uri)));
    }
}
