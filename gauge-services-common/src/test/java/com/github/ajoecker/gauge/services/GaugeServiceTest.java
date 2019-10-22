package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.services.login.BasicAuthentication;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.Table;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GaugeServiceTest {
    BasicAuthentication loginHandler = new BasicAuthentication(new VariableAccessor());

    @Test
    public void postWithoutVariablesArePassedToConnector() {
        final String theQuery = "this is a theQuery";
        Connector connector = new Connector() {
            @Override
            public void postWithLogin(String query, String variables, LoginHandler loginHandler) {
                assertAll(
                        () -> assertEquals(query, query),
                        () -> assertEquals("", variables)
                );
            }
        };
        new GaugeService(connector, loginHandler).posting(theQuery);
    }

    @Test
    public void getWithoutVariablesArePassedToConnector() {
        final String theQuery = "1234";
        Connector connector = new Connector() {
            @Override
            public void getWithLogin(String query, LoginHandler loginHandler) {
                assertEquals(theQuery, query);
            }
        };
        new GaugeService(connector, loginHandler).get(theQuery);
    }

    @Test
    public void statusCodeIsChecked() {
        final int code = 200;
        Connector connector = new Connector() {
            @Override
            public void verifyStatusCode(int expected) {
                assertEquals(code, expected);
            }
        };
        new GaugeService(connector, loginHandler).verifyStatusCode(code);
    }

    @Test
    public void postWithVariablesAsTableNoReplacement() {
        Connector connector = new Connector() {
            @Override
            public void postWithLogin(String query, String variables, LoginHandler loginHandler) {
                assertAll(
                        () -> assertEquals("simple", query),
                        () -> assertEquals("", variables)
                );
            }
        };
        Table table = new Table(List.of("foo", "bar"));
        table.addRow(List.of("fooValue", "barValue"));
        new GaugeService(connector, loginHandler).postingWithVariables("simple", table);
    }

    @Test
    public void postWithVariablesAsTableWithReplacement() {
        Connector connector = new Connector() {
            @Override
            public void postWithLogin(String query, String variables, LoginHandler loginHandler) {
                assertAll(
                        () -> assertEquals("fooValue : barValue", query),
                        () -> assertEquals("", variables)
                );
            }
        };
        Table table = new Table(List.of("name", "value"));
        table.addRow(List.of("foo", "fooValue"));
        table.addRow(List.of("bar", "barValue"));
        new GaugeService(connector, loginHandler).postingWithVariables("%foo% : %bar%", table);
    }

    @Test
    public void postWithVariablesAsStringWithReplacement() {
        Connector connector = new Connector() {
            @Override
            public void postWithLogin(String query, String variables, LoginHandler loginHandler) {
                assertAll(
                        () -> assertEquals("fooValue : barValue", query),
                        () -> assertEquals("", variables)
                );
            }
        };
        String replacement = "foo:fooValue,bar:barValue";
        new GaugeService(connector, loginHandler).postingWithVariables("%foo% : %bar%", replacement);
    }

    @Test
    @Disabled
    public void postWithVariablesAsMapWithReplacement() {
        Connector connector = new Connector() {
            @Override
            public void postWithLogin(String query, String variables, LoginHandler loginHandler) {
                assertAll(
                        () -> assertEquals("foo : bar", query),
                        () -> assertEquals("{foo:fooValue,bar:barValue}", variables)
                );
            }
        };
        String replacement = "{foo:fooValue,bar:barValue}";
        new GaugeService(connector, loginHandler).postingWithVariables("foo : bar", replacement);
    }

    @Test
    public void verifyContainsWithTable() {
        Table table = new Table(List.of("name", "nationality"));
        table.addRow(List.of("Hans", "German"));
        table.addRow(List.of("Alicia", "Spain"));

        Consumer<Object[]> consumer = objects -> {
            assertAll(
                    () -> assertEquals(2, objects.length),
                    () -> assertEquals(objects[0], Map.of("nationality", "German", "name", "Hans")),
                    () -> assertEquals(objects[1], Map.of("nationality", "Spain", "name", "Alicia"))
            );
        };
        Connector connector = new Connector() {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        new GaugeService(connector, loginHandler).thenContains("path", table);
    }

    @Test
    public void verifyContainsWithMap() {
        String map = "{name: Hans, nationality: German}, {name: Alicia, nationality: Spain}";
        Consumer<Object[]> consumer = objects -> {
            assertAll(
                    () -> assertEquals(2, objects.length ),
                    () -> assertEquals(objects[0], Map.of("nationality", "German", "name", "Hans")),
                    () -> assertEquals(objects[1], Map.of("nationality", "Spain", "name", "Alicia"))
            );
        };
        Connector connector = new Connector() {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        new GaugeService(connector, loginHandler).thenContains("path", map);
    }

    @Test
    public void verifyContainsWithString() {
        String string = "Hans, Alicia";
        Consumer<Object[]> consumer = objects -> {
            assertAll(
                    () -> assertEquals(2, objects.length),
                    () -> assertEquals("Hans", objects[0]),
                    () -> assertEquals("Alicia", objects[1])
            );
        };
        Connector connector = new Connector() {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        new GaugeService(connector, loginHandler).thenContains("path", string);
    }
}
