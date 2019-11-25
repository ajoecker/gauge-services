package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VerificationTest {
    @Test
    public void isWithString() {
        Consumer<Object[]> consumer = objects -> Assertions.assertArrayEquals(new String[]{"Pablo Picasso", "Banksy"}, objects);
        Connector connector = new Connector() {
            @Override
            public Consumer<Object[]> thenIs(String dataPath) {
                return consumer;
            }
        };
        Registry.init(connector);
        Verification verification = new Verification();
        verification.thenIs("foo", "Pablo Picasso, Banksy");
    }

    @Test
    public void isWithTable() {
        Map<String, String> m = Map.of("name", "foo", "value", "fooValue");
        Consumer<Object[]> consumer = objects -> Assertions.assertArrayEquals(new Map[]{m}, objects);
        Connector connector = new Connector() {
            @Override
            public Consumer<Object[]> thenIs(String dataPath) {
                return consumer;
            }
        };
        Registry.init(connector);
        Verification verification = new Verification();
        Table table = new Table(List.of("name", "value"));
        table.addRow(List.of("foo", "fooValue"));
        verification.thenIs("fooPath", table);
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
}