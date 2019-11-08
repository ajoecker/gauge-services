package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoginTest {
    @Test
    public void onlyTokenGivenIsThenUsed() {
        VariableAccessor variableAccessor = new VariableAccessor() {
            @Override
            public String token() {
                return "wrong-token";
            }
        };
        LoginHandler loginHandler = new LoginHandler() {
            @Override
            public void setLogin(RequestSpecification request) {
            }

            @Override
            public void loginWithGivenCredentials(String user, String password, Connector connector) {
            }

            @Override
            public void loginWithSystemCredentials(Connector connector) {
                Assertions.assertEquals("funny-token", connector.getVariableAccessor().token());
            }
        };
        Registry.init(new Connector(variableAccessor), loginHandler);
        new Login().loginWithToken("funny-token");
    }
}
