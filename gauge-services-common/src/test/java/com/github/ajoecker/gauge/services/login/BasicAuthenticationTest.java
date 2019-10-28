package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.specification.AuthenticationSpecification;
import io.restassured.specification.PreemptiveAuthSpec;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class BasicAuthenticationTest {
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
    public void givenCredentialsAreSet() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials("user", "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(preemptiveAuthSpec).basic("user", "password");
    }

    @Test
    public void noUserGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials(null, "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(preemptiveAuthSpec, never()).basic(null, "password");
    }

    @Test
    public void noPasswordGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication(new VariableAccessor());
        basicAuthentication.loginWithGivenCredentials("user", null, null);
        basicAuthentication.setLogin(requestSpecification);
        Mockito.verify(preemptiveAuthSpec, never()).basic("user", "password");
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
        Mockito.verify(preemptiveAuthSpec).basic("user", "password");
    }
}
