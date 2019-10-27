package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.google.common.base.Strings;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
    private VariableAccessor variableAccessor;

    public TokenBasedLogin(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
    }

    @Override
    public void setLogin(RequestSpecification request) {
        if (!Strings.isNullOrEmpty(loginToken)) {
            request.auth().preemptive().oauth2(loginToken);
        }
    }

    @Override
    public void loginWithSystemCredentials(Connector connector) {
        loginToken = Optional.ofNullable(variableAccessor.token())
                .orElseGet(() -> sendLoginQuery(connector, UnaryOperator.identity()));
    }

    @Override
    public void loginWithGivenCredentials(String user, String password, Connector connector) {
        loginToken = sendLoginQuery(connector, s -> replaceVariablesInQuery(s, "user:" + user + separator() + "password:" + password, connector));
    }

    private String sendLoginQuery(Connector connector, UnaryOperator<String> queryMapper) {
        try {
            connector.post(readQuery(queryMapper));
            return connector.extract(variableAccessor.tokenPath());
        } catch (URISyntaxException | IOException e) {
            throw new QueryException(e);
        }
    }

    private String readQuery(UnaryOperator<String> mapper) throws IOException, URISyntaxException {
        String queryFile = "/" + variableAccessor.tokenQueryFile();
        URI uri = TokenBasedLogin.class.getResource(queryFile).toURI();
        return mapper.apply(readString(Paths.get(uri)));
    }

    public static class QueryException extends RuntimeException {
        public QueryException(Throwable throwable) {
            super(throwable);
        }
    }
}
