package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetTest {
    @Test
    public void getWithParametersTable() {
        Table table = new Table(List.of("name", "value"));
        table.addRow(List.of("Hans", "German"));
        table.addRow(List.of("Alicia", "Spain"));
        Connector connector = new Connector() {
            @Override
            public void getWithLogin(String query, String parameter, LoginHandler loginHandler) {
                assertAll(() -> assertEquals("Hans=German&Alicia=Spain", parameter),
                        () -> assertEquals("foo", query));
            }
        };
        Registry.init(connector);
        new GET().gettingWithVariables("foo", table);
    }
}
