package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.TestVariableStorage;
import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtractTest {
    private TestVariableStorage testVariableStorage;
    private Sender sender;

    @BeforeEach
    public void before() {
        testVariableStorage = new TestVariableStorage();
        sender = new Sender(new VariableAccessor());
    }

    @Test
    public void extract() {
        initConnector(new Connector(testVariableStorage, sender), "");
        new Extract().extractPath("token", "id=5");
        assertExtractToken("foo");
    }

    @Test
    public void extractWithQueryReplacement() {
        testVariableStorage.put("id", "2");
        initConnector(new Connector(testVariableStorage, sender), "");
        new Extract().extractPath("token", "id=%id%");
        assertExtractToken("bar");
    }

    private void assertExtractToken(String expected) {
        assertThat(testVariableStorage.get("token")).contains(expected);
    }

    private static void initConnector(Connector connector, String path) {
        Registry.init(connector);
        ExtractableResponse extractableResponse = mock(ExtractableResponse.class);
        ValidatableResponse validatableResponse = mock(ValidatableResponse.class);
        Response response = mock(Response.class);
        when(response.then()).thenReturn(validatableResponse);
        when(validatableResponse.extract()).thenReturn(extractableResponse);
        when(extractableResponse.path(path)).thenReturn(
                List.of(
                        Map.of("id", "5", "token", "foo"),
                        Map.of("id", "2", "token", "bar")
                )
        );
        connector.setResponse(response);
    }
}
