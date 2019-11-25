package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.TestVariableStorage;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class PostTest {
    @Test
    public void postWithoutVariablesArePassedToConnector() {
        final String theQuery = "this is a theQuery";
        Sender sender = new Sender(new VariableAccessor()) {
            @Override
            public Response sendPost(AuthenticationHandler loginHandler, String endpoint, Object body) {
                assertEquals(theQuery, body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(new TestVariableStorage(), sender);
        Registry.init(connector);
        new POST().posting(theQuery);
    }

    @Test
    public void postWithQueryReplacement() {
        final String theQuery = "this is %foo%";
        TestVariableStorage testVariableStorage = new TestVariableStorage();
        testVariableStorage.put("foo", "1");
        Sender sender = new Sender(new VariableAccessor()) {
            @Override
            public Response sendPost(AuthenticationHandler loginHandler, String endpoint, Object body) {
                assertEquals("this is 1", body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(testVariableStorage, sender);
        Registry.init(connector);
        new POST().posting(theQuery);
    }

    @Test
    public void postWithQueryReplacementFromLatesResponse() {
        final String theQuery = "this is %foo%";
        TestVariableStorage testVariableStorage = new TestVariableStorage();
        Sender sender = new Sender(new VariableAccessor()) {
            @Override
            public Response sendPost(AuthenticationHandler loginHandler, String endpoint, Object body) {
                assertEquals("this is 1", body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(testVariableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                return variablePath.equals("foo") ? Optional.of("1") : Optional.empty();
            }
        };
        Registry.init(connector);
        new POST().posting(theQuery);
    }
}
