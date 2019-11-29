package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.*;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
