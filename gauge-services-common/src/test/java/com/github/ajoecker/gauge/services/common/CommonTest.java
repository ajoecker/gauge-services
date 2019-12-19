package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.*;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonTest {
    @Test
    public void statusCodeIsChecked() {
        final int code = 200;
        Sender sender = new Sender(new VariableAccessor()) {
            public void verifyStatusCode(int expected) {
                assertEquals(code, expected);
            }
        };
        Connector connector = new Connector(sender);
        Registry.get().init("foo", sender, connector, null);
        new Common().verifyStatusCode(code);
    }

    @Test
    public void onlyTokenGivenIsThenUsed() {
        VariableAccessor variableAccessor = new VariableAccessor() {
            @Override
            public String token() {
                return "wrong-token";
            }
        };
        Sender sender = new Sender(variableAccessor);
        Connector connector = new Connector(new TestVariableStorage(), sender);
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void setLogin(RequestSpecification request) {
            }

            @Override
            public void loginWithToken(String token) {
                Assertions.assertEquals("funny-token", token);
            }
        };
        Registry.get().init("foo", sender, connector, authenticationHandler);
        new Common().loginWithToken("funny-token");
    }

    @Test
    public void sum() {
        Sender sender = new Sender(new VariableAccessor());
        TestVariableStorage variableStorage = new TestVariableStorage();
        variableStorage.put("val1", 43.54f);
        Connector connector = new Connector(variableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                if (variablePath.equals("val2")) {
                    return Optional.of("32");
                }
                return super.fromLatestResponse(variablePath);
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        new Common().extractAsSum("foobar", "val1, val2");
        org.assertj.core.api.Assertions.assertThat(variableStorage.get("foobar")).contains(75.54);
    }

    @Test
    public void sumAllNull() {
        Sender sender = new Sender(new VariableAccessor());
        TestVariableStorage variableStorage = new TestVariableStorage();
        Connector connector = new Connector(variableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                return Optional.empty();
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        new Common().extractAsSum("foobar", "val1, val2");
        org.assertj.core.api.Assertions.assertThat(variableStorage.get("foobar")).contains(0.0);
    }

    @Test
    public void sumPartiallyNull() {
        Sender sender = new Sender(new VariableAccessor());
        TestVariableStorage variableStorage = new TestVariableStorage();
        variableStorage.put("val1", 43.54);
        Connector connector = new Connector(variableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                return Optional.empty();
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        new Common().extractAsSum("foobar", "val1, val2");
        org.assertj.core.api.Assertions.assertThat(variableStorage.get("foobar")).contains(43.54);
    }

    @Test
    public void sumWithEmptyString() {
        Sender sender = new Sender(new VariableAccessor());
        TestVariableStorage variableStorage = new TestVariableStorage();
        variableStorage.put("val1", "");
        Connector connector = new Connector(variableStorage, sender) {
            @Override
            public Optional<Object> fromLatestResponse(String variablePath) {
                return Optional.empty();
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        new Common().extractAsSum("foobar", "val1, val2");
        org.assertj.core.api.Assertions.assertThat(variableStorage.get("foobar")).contains(0.0);
    }
}
