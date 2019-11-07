package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.specification.AuthenticationSpecification;
import io.restassured.specification.PreemptiveAuthSpec;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class TokenBasedLoginTest {
    private RequestSpecification requestSpecification;
    private PreemptiveAuthSpec preemptiveAuthSpec;

    @BeforeEach
    public void setup() {
        requestSpecification = Mockito.mock(RequestSpecification.class);
        AuthenticationSpecification authenticationSpecification = mock(AuthenticationSpecification.class);
        preemptiveAuthSpec = mock(PreemptiveAuthSpec.class);
        when(requestSpecification.auth()).thenReturn(authenticationSpecification);
        when(authenticationSpecification.preemptive()).thenReturn(preemptiveAuthSpec);
    }

    @Test
    public void tokenIsGivenNoQueryIsRequired() {
        TokenBasedLogin tokenBasedLogin = new TokenBasedLogin();
        tokenBasedLogin.loginWithSystemCredentials(new Connector() {
            @Override
            public VariableAccessor getVariableAccessor() {
                return new VariableAccessor() {
                    @Override
                    public String token() {
                        return "12345";
                    }
                };
            }
        });
        tokenBasedLogin.setLogin(requestSpecification);
        verify(preemptiveAuthSpec).oauth2("12345");
    }
}
