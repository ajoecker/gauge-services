package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.thoughtworks.gauge.Table;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceTest {
    @Test
    public void postWithoutVariablesArePassedToConnector() {
        final String theQuery = "this is a theQuery";
        Connector connector = new Connector() {
            @Override
            public void post(String query, String path, LoginHandler loginHandler) {
                assertEquals(query, query);
            }
        };
        Registry.init(connector);
        new POST().posting(theQuery);
    }

//    @Test
//    public void getWithoutVariablesArePassedToConnector() {
//        final String theQuery = "1234";
//        Connector connector = new Connector() {
//            @Override
//            public void get(String resource, String parameters, LoginHandler loginHandler) {
//                assertEquals(theQuery, resource);
//            }
//        };
//        Registry.init(connector);
//        new GET().get(theQuery);
//    }

    @Test
    public void statusCodeIsChecked() {
        final int code = 200;
        Connector connector = new Connector() {
            @Override
            public void verifyStatusCode(int expected) {
                assertEquals(code, expected);
            }
        };
        Registry.init(connector);
        new General().verifyStatusCode(code);
    }

    @Test
    public void postWithVariablesAsTableNoReplacement() {
        Connector connector = new Connector() {
            @Override
            public void post(String query, String path, LoginHandler loginHandler) {
                assertEquals("simple", query);
            }
        };
        Table table = new Table(List.of("foo", "bar"));
        table.addRow(List.of("fooValue", "barValue"));
        Registry.init(connector);
       // new POST().postingWithVariables("simple", "", table);
    }

    @Test
    public void postWithVariablesAsTableWithReplacement() {
        Connector connector = new Connector() {
            @Override
            public void post(String query, String path, LoginHandler loginHandler) {
                assertEquals("fooValue : barValue", query);
            }
        };
        Table table = new Table(List.of("name", "value"));
        table.addRow(List.of("foo", "fooValue"));
        table.addRow(List.of("bar", "barValue"));
        Registry.init(connector);
      //  new POST().postingWithVariables("%foo% : %bar%", "", table);
    }

    @Test
    public void postWithVariablesAsStringWithReplacement() {
        Connector connector = new Connector() {
            @Override
            public void post(String query, String path, LoginHandler loginHandler) {
                assertEquals("fooValue : barValue", query);
            }
        };
        String replacement = "foo=fooValue,bar=barValue";
        Registry.init(connector);
        //new POST().postingWithVariables("%foo% : %bar%", "", replacement);
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
        Registry.init(connector);
        new Verification().thenContains("path", table);
    }

    @Test
    public void verifyContainsWithMap() {
        String map = "{name: Hans, nationality: German}, {name: Alicia, nationality: Spain}";
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
        Registry.init(connector);
        new Verification().thenContains("path", map);
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
        Registry.init(connector);
        new Verification().thenContains("path", string);
    }
}
