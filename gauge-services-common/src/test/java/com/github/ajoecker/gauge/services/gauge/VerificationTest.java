package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
}
