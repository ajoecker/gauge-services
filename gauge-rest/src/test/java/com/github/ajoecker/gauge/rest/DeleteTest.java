package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.common.RequestSender;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

public class DeleteTest {
    VariableAccessor variableAccessor = new VariableAccessor() {
        @Override
        public String endpoint() {
            return "http://endpoint";
        }
    };

    @Test
    public void foo() {
        RequestSender requestSender = new RequestSender(variableAccessor) {
            @Override
            public Response sendDelete(LoginHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/customers/4");
                return Mockito.mock(Response.class);
            }
        };
        RestConnector connector = new RestConnector(new TestVariableStorage(), requestSender);
        Registry.init(connector);
        Delete delete = new Delete();
        delete.delete("4", "de/customers");
    }

    @Test
    public void bar() {
        RequestSender requestSender = new RequestSender(variableAccessor) {
            @Override
            public Response sendDelete(LoginHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/customers/4");
                return Mockito.mock(Response.class);
            }
        };
        TestVariableStorage variableStorage = new TestVariableStorage();
        variableStorage.put("foo", 4);
        RestConnector connector = new RestConnector(variableStorage, requestSender);
        Registry.init(connector);
        Delete delete = new Delete();
        delete.delete("%foo%", "de/customers");
    }

    @Test
    public void blub() {
        RequestSender requestSender = new RequestSender(variableAccessor) {
            @Override
            public Response sendDelete(LoginHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/cua/4");
                return Mockito.mock(Response.class);
            }
        };
        TestVariableStorage variableStorage = new TestVariableStorage();
        RestConnector connector = new RestConnector(variableStorage, requestSender) {
            @Override
            public Optional<Object> pathFromPreviousResponse(String variablePath) {
                return variablePath.equals("customers") ? Optional.of("cua") : Optional.empty();
            }
        };
        Registry.init(connector);
        Delete delete = new Delete();
        delete.delete("4", "de/%customers%");
    }
}
