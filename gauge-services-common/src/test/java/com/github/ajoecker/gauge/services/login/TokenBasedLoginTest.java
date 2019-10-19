package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.specification.AuthenticationSpecification;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenBasedLoginTest {
    private AuthenticationSpecification authenticationSpecification;
    private RequestSpecification requestSpecification;

    @BeforeEach
    public void setup() {
        requestSpecification = Mockito.mock(RequestSpecification.class);
        authenticationSpecification = Mockito.mock(AuthenticationSpecification.class);
        when(requestSpecification.auth()).thenReturn(authenticationSpecification);
    }

    @Test
    public void tokenIsGivenNNoQueryIsRequired() {
        TokenBasedLogin tokenBasedLogin = new TokenBasedLogin(new VariableAccessor() {
            @Override
            public String token() {
                return "12345";
            }
        });
        tokenBasedLogin.loginWithSystemCredentials(null);
        tokenBasedLogin.setLogin(requestSpecification);
        verify(authenticationSpecification).oauth2("12345");
    }
}
