package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.*;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
        new Common().extractPath("token", "id=5");
        assertExtractToken("foo");
    }

    @Test
    public void extractWIthoutAttributeMapping() {
        initConnectorSingle(new Connector(testVariableStorage, sender), "");
        new Common().extractPath("token");
        assertExtractToken("foo");
    }

    @Test
    public void extractWithQueryReplacement() {
        testVariableStorage.put("id", "2");
        initConnector(new Connector(testVariableStorage, sender), "");
        new Common().extractPath("token", "id=%id%");
        assertExtractToken("bar");
    }

    @Test
    public void extractJson() {
        Sender sender = new Sender(new VariableAccessor()) {
            @Override
            public Object path(String path) {
                return "{ \n" +
                        "   \"id\":44054,\n" +
                        "   \"contractIdentifier\":\"GER-Q-KR-0000044054\",\n" +
                        "   \"paymentInterval\":\"monthly\",\n" +
                        "   \"startOfInsurance\":\"2020-01-01\",\n" +
                        "   \"price\":{ \n" +
                        "      \"formatted\":\"32,56\",\n" +
                        "      \"cents\":3256,\n" +
                        "      \"currency\":\"EUR\"\n" +
                        "   }\n" +
                        "}";
            }
        };
        Registry.get().init("foo", s -> new Connector(testVariableStorage, sender));
        new Common().extractFromJson("price.formatted", "token", "jsonPath");
        assertExtractToken("32,56");
    }

    private void assertExtractToken(String expected) {
        assertThat(testVariableStorage.get("token")).contains(expected);
    }

    private void initConnector(Connector connector, String path) {
        Registry.get().init("foo", s -> connector);
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
        sender.setResponse(response);
    }

    private void initConnectorSingle(Connector connector, String path) {
        Registry.get().init("foo", s -> connector);
        ExtractableResponse extractableResponse = mock(ExtractableResponse.class);
        ValidatableResponse validatableResponse = mock(ValidatableResponse.class);
        Response response = mock(Response.class);
        when(response.then()).thenReturn(validatableResponse);
        when(validatableResponse.extract()).thenReturn(extractableResponse);
        when(extractableResponse.path(path)).thenReturn(Map.of("id", "5", "token", "foo"));
        sender.setResponse(response);
    }
}
