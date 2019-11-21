package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthenticationTest {
    @Test
    public void onlyTokenGivenIsThenUsed() {
        VariableAccessor variableAccessor = new VariableAccessor() {
            @Override
            public String token() {
                return "wrong-token";
            }
        };
        Connector connector = new Connector(variableAccessor, VariableStorage.create());
        LoginHandler loginHandler = new LoginHandler() {
            @Override
            public void setLogin(RequestSpecification request) {
            }

            @Override
            public void loginWithUserPassword(String user, String password, Connector connector) {
            }

            @Override
            public void loginWithToken(String token) {
                Assertions.assertEquals("funny-token", token);
            }
        };
        Registry.init(connector, loginHandler);
        new Authentication().loginWithToken("funny-token");
    }
}
