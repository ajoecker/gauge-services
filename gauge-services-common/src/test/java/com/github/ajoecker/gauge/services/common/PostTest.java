package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.TestVariableStorage;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.common.General;
import com.github.ajoecker.gauge.services.common.POST;
import com.github.ajoecker.gauge.services.common.RequestSender;
import com.github.ajoecker.gauge.services.common.Verification;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.Table;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.crypto.spec.OAEPParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostTest {
    @Test
    public void postWithoutVariablesArePassedToConnector() {
        final String theQuery = "this is a theQuery";
        RequestSender sender = new RequestSender(new VariableAccessor()) {
            @Override
            public Response sendPost(LoginHandler loginHandler, String endpoint, Object body) {
                assertEquals(theQuery, body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(new TestVariableStorage(), sender);
        Registry.init(connector);
        new POST().posting(theQuery);
    }

    @Test
    public void foo() {
        final String theQuery = "this is %foo%";
        TestVariableStorage testVariableStorage = new TestVariableStorage();
        testVariableStorage.put("foo", "1");
        RequestSender sender = new RequestSender(new VariableAccessor()) {
            @Override
            public Response sendPost(LoginHandler loginHandler, String endpoint, Object body) {
                assertEquals("this is 1", body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(testVariableStorage, sender);
        Registry.init(connector);
        new POST().posting(theQuery);
    }

    @Test
    public void blu() {
        final String theQuery = "this is %foo%";
        TestVariableStorage testVariableStorage = new TestVariableStorage();
        RequestSender sender = new RequestSender(new VariableAccessor()) {
            @Override
            public Response sendPost(LoginHandler loginHandler, String endpoint, Object body) {
                assertEquals("this is 1", body);
                return Mockito.mock(Response.class);
            }
        };
        Connector connector = new Connector(testVariableStorage, sender) {
            @Override
            public Optional<Object> pathFromPreviousResponse(String variablePath) {
                return variablePath.equals("foo") ? Optional.of("1") : Optional.empty();
            }
        };
        Registry.init(connector);
        new POST().posting(theQuery);
    }
}
