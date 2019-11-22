package com.github.ajoecker.gauge.services.login;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.TestVariableStorage;
import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.common.RequestSender;
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
        BasicAuthentication basicAuthentication = new BasicAuthentication();
        basicAuthentication.loginWithUserPassword("user", "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(preemptiveAuthSpec).basic("user", "password");
    }

    @Test
    public void noUserGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication();
        basicAuthentication.loginWithUserPassword(null, "password", null);
        basicAuthentication.setLogin(requestSpecification);
        verify(preemptiveAuthSpec, never()).basic(null, "password");
    }

    @Test
    public void noPasswordGivenDoesNotSetCredentials() {
        BasicAuthentication basicAuthentication = new BasicAuthentication();
        basicAuthentication.loginWithUserPassword("user", null, null);
        basicAuthentication.setLogin(requestSpecification);
        Mockito.verify(preemptiveAuthSpec, never()).basic("user", "password");
    }

    @Test
    public void credentialsLoadFromSysEnv() {
        BasicAuthentication basicAuthentication = new BasicAuthentication();
        VariableAccessor variableAccessor = new VariableAccessor() {
            @Override
            public String user() {
                return "user";
            }

            @Override
            public String password() {
                return "password";
            }
        };
        RequestSender requestSender = new RequestSender(variableAccessor);
        new Connector(new TestVariableStorage(), requestSender);
        basicAuthentication.loginWithSystemCredentials(requestSender);
        basicAuthentication.setLogin(requestSpecification);
        Mockito.verify(preemptiveAuthSpec).basic("user", "password");
    }
}
