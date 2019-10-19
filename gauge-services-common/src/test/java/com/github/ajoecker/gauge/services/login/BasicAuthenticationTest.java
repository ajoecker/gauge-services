package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.specification.AuthenticationSpecification;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class BasicAuthenticationTest {
    private AuthenticationSpecification authenticationSpecification;
    private RequestSpecification requestSpecification;

    @BeforeEach
    public void setup() {
        requestSpecification = Mockito.mock(RequestSpecification.class);
        authenticationSpecification = Mockito.mock(AuthenticationSpecification.class);
        when(requestSpecification.auth()).thenReturn(authenticationSpecification);
    }

    @Test
    public void givenCredentialsAreSet() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials("user", "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(authenticationSpecification).basic("user", "password");
    }

    @Test
    public void noUserGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials(null, "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(authenticationSpecification, never()).basic(null, "password");
    }

    @Test
    public void noPasswordGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials("user", null, null);
        basicAuthentication.setLogin(requestSpecification);
        Mockito.verify(authenticationSpecification, never()).basic("user", "password");
    }

    @Test
    public void credentialsLoadFromSysEnv() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor() {
            @Override
            public String user() {
                return "user";
            }

            @Override
            public String password() {
                return "password";
            }
        });
        basicAuthentication.loginWithSystemCredentials(null);
        basicAuthentication.setLogin(requestSpecification);
        Mockito.verify(authenticationSpecification).basic("user", "password");
    }
}
