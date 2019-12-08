package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.*;
import com.thoughtworks.gauge.Table;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class VerificationTest {
    private Sender sender = new Sender(new VariableAccessor());

    @Test
    public void isWithString() {
        Consumer<Object[]> consumer = objects -> Assertions.assertArrayEquals(new String[]{"Pablo Picasso", "Banksy"}, objects);
        Connector connector = new Connector(sender) {
            @Override
            public Consumer<Object[]> thenIs(String dataPath) {
                return consumer;
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        Verification verification = new Verification();
        verification.thenIs("foo", "Pablo Picasso, Banksy");
    }

    @Test
    public void isWithTable() {
        Map<String, String> m = Map.of("name", "foo", "value", "fooValue");
        Consumer<Object[]> consumer = objects -> Assertions.assertArrayEquals(new Map[]{m}, objects);
        Connector connector = new Connector(sender) {
            @Override
            public Consumer<Object[]> thenIs(String dataPath) {
                return consumer;
            }
        };
        Registry.get().init("foo", sender1 -> connector);
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
        Connector connector = new Connector(sender) {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        Registry.get().init("foo", sender1 -> connector);
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
        Connector connector = new Connector(sender) {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        Registry.get().init("foo", sender1 -> connector);
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
        Connector connector = new Connector(sender) {
            @Override
            public Consumer<Object[]> thenContains(String dataPath) {
                return consumer;
            }
        };
        Registry.get().init("foo", sender1 -> connector);
        new Verification().thenContains("path", map);
    }

    @Test
    public void verifiesJson() {
        String s = "{" +
                "   \"id\": 34," +
                "   \"name\": \"foobar\"" +
                "}";

        Response response = Mockito.mock(Response.class);
        ResponseBody responseBody = Mockito.mock(ResponseBody.class);
        Mockito.when(response.body()).thenReturn(responseBody);
        Mockito.when(responseBody.asString()).thenReturn(s);
        Connector connector = new Connector(sender);
        sender.setResponse(response);
        Registry.get().init("foo", sender1 -> connector);
        new Verification().thenIsEqual(s);
    }

    @Test
    public void equalVariables() {
        VariableStorage storage = new TestVariableStorage();
        storage.put("foo", 2.34);
        storage.put("bar", 2.34);
        Connector connector = new Connector(storage, sender);
        Registry.get().init("foo", sender1 -> connector);
        new Verification().compareVariables("foo", "bar");
    }

    @Test
    public void equalMultipleVariables() {
        VariableStorage storage = new TestVariableStorage();
        storage.put("foo", 2.34);
        storage.put("bar", 2.34);
        storage.put("blub", 2.34);
        Connector connector = new Connector(storage, sender);
        Registry.get().init("foo", sender1 -> connector);
        new Verification().compareVariables("foo", "bar, blub  ");
    }

    @Test
    public void equalWrongVariables() {
        VariableStorage storage = new TestVariableStorage();
        storage.put("foo", 2.34);
        storage.put("bar", 2.35);
        storage.put("blub", 2.34);
        storage.put("brab", 3.34);
        Connector connector = new Connector(storage, sender);
        Registry.get().init("foo", sender1 -> connector);
        try {
            new Verification().compareVariables("foo", "bar, blub,brab  ");
            fail("should have failed, as values are different");
        } catch (AssertionError e) {
            // thats what we want
            org.assertj.core.api.Assertions.assertThat(e.getMessage()).contains("2.35", "3.34");
        }
    }

    @Test
    public void equalNonExistingVariables() {
        VariableStorage storage = new TestVariableStorage();
        storage.put("foo", 2.34);
        Connector connector = new Connector(storage, sender);
        Registry.get().init("foo", sender1 -> connector);
        try {
            new Verification().compareVariables("foo", "bar, blub,brab  ");
            fail("should have failed, as values are not present");
        } catch (AssertionError e) {
            // thats what we want
        }
    }
}
