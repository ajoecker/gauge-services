package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.Sender;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
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
    public void delete() {
        Sender sender = new Sender(variableAccessor) {
            @Override
            public Response sendDelete(AuthenticationHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/customers/4");
                return Mockito.mock(Response.class);
            }
        };
        Registry.get().init("foo", sender1 -> new RestConnector(new TestVariableStorage(), sender));
        Delete delete = new Delete();
        delete.delete("4", "de/customers");
    }

    @Test
    public void deleteWithQueryReplacement() {
        Sender sender = new Sender(variableAccessor) {
            @Override
            public Response sendDelete(AuthenticationHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/customers/4");
                return Mockito.mock(Response.class);
            }
        };
        TestVariableStorage variableStorage = new TestVariableStorage();
        variableStorage.put("foo", 4);
        Registry.get().init("bar", sender1 -> new RestConnector(variableStorage, sender));
        Delete delete = new Delete();
        delete.delete("%foo%", "de/customers");
    }

    @Test
    public void deleteWithQueryReplacementFromLatestResponse() {
        Sender sender = new Sender(variableAccessor) {
            @Override
            public Response sendDelete(AuthenticationHandler loginHandler, String deletePath) {
                Assertions.assertThat(deletePath).isEqualTo("http://endpoint/de/cua/4");
                return Mockito.mock(Response.class);
            }
        };
        TestVariableStorage variableStorage = new TestVariableStorage();
        Registry.get().init("bar", sender1 -> new RestConnector(variableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                return variablePath.equals("customers") ? Optional.of("cua") : Optional.empty();
            }
        });
        Delete delete = new Delete();
        delete.delete("4", "de/%customers%");
    }
}
