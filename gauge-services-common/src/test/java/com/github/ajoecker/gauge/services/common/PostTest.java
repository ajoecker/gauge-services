package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.*;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.thoughtworks.gauge.Table;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
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
        Registry.get().init("foo", sender, connector, null);
        new Common().useEndpoint("http://endpoint");
        new Common().posting(theQuery);
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
        Registry.get().init("foo", sender, connector, null);
        new Common().useEndpoint("http://endpoint");
        new Common().posting(theQuery);
    }

    @Test
    public void postWithQueryReplacementFromTable() {
        final String theQuery = "this is %foo%";
        Sender sender = new Sender(new VariableAccessor()) {
            @Override
            public Response sendPost(AuthenticationHandler loginHandler, String endpoint, Object body) {
                assertEquals("this is 1", body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(new TestVariableStorage(), sender);
        Registry.get().init("foo", sender, connector, null);
        new Common().useEndpoint("http://endpoint");
        Table theTable = new Table(List.of("variable", "value"));
        theTable.addRow(List.of("foo", "1"));
        new Common().postingWithParameters(theQuery, theTable);
    }

    @Test
    public void postWithQueryReplacementFromLatestResponse() {
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
        Registry.get().init("foo", sender, connector, null);
        new Common().useEndpoint("http://endpoint");
        new Common().posting(theQuery);
    }
}
