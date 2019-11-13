package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.ConfigurationSource;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.VariableAccessor;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceUtilTest {
    private VariableStorage variableStorage = new VariableStorage() {
        Map<String, Object> container = new HashMap<>();

        @Override
        public void put(String key, Object value) {
            container.put(key, value);
        }

        @Override
        public Object get(String key) {
            return container.get(key);
        }
    };

    @Test
    public void recognisesMap() {
        String mapString = "{name: Banksy, nationality: British}";
        Assertions.assertTrue(isMap(mapString));
    }

    @Test
    public void ignoresNonMap() {
        String mapString = "whatever";
        Assertions.assertFalse(isMap(mapString));
    }

    @Test
    public void parsesSingleMap() {
        String mapString = "{name: Banksy, nationality: British}";
        List<Map<String, String>> parse = ServiceUtil.parseMap(mapString);
        assertEquals(parse, List.of(Map.of("name", "Banksy", "nationality", "British")));
    }

    @Test
    public void parsesMultiMap() {
        String mapString = "{name: Banksy, nationality: British}, {name: Pablo Picasso, nationality: Spanish}";
        List<Map<String, String>> parse = ServiceUtil.parseMap(mapString);
        assertEquals(parse, List.of(
                Map.of("name", "Banksy", "nationality", "British"),
                Map.of("name", "Pablo Picasso", "nationality", "Spanish")
        ));
    }

    @Test
    public void replaceVariablesInQueryWorks() {
        String query = "{\n" +
                "    popular_artists(size: %size%) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String queryReplaced = "{\n" +
                "    popular_artists(size: 2) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        assertEquals(ServiceUtil.replaceVariablesInQuery(query, "size=2", new Connector()), queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithVariablesWorks() {
        String query = "{\n" +
                "    popular_artists(size: %size%) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String queryReplaced = "{\n" +
                "    popular_artists(size: 2) {\n" +
                "        artists {\n" +
                "            name\n" +
                "            nationality\n" +
                "        }\n" +
                "    }\n" +
                "}";
        ExtractableResponse<Response> re = Mockito.mock(ExtractableResponse.class);
        Mockito.when(re.path("foo")).thenReturn(2);

        Connector connector = new Connector();
        connector.setPreviousResponse(re);
        assertEquals(ServiceUtil.replaceVariablesInQuery(query, "size=%foo%", connector), queryReplaced);
    }

    @Test
    public void splitCorrectly() {
        List<String> strings = splitIntoKeyValueList("last_name=Doe,first_name=John");
        assertThat(strings).containsExactly("last_name", "Doe", "first_name", "John");
    }

    @Test
    public void replaceSingleVariable() {
        variableStorage.put("lastname", "Miller");
        Connector connector = new Connector(new VariableAccessor(), variableStorage);
        assertThat(replaceVariables("%lastname%", connector)).isEqualTo("Miller");
    }

    @Test
    public void replaceVariableInResource() {
        variableStorage.put("id", "4");
        Connector connector = new Connector(new VariableAccessor(), variableStorage);
        assertThat(replaceVariables("cases/%id%", connector)).isEqualTo("cases/4");
    }

    @Test
    public void replaceVariableInResource02() {
        variableStorage.put("id", "4");
        Connector connector = new Connector(new VariableAccessor(), variableStorage);
        assertThat(replaceVariables("cases/%id%/other", connector)).isEqualTo("cases/4/other");
    }

    @Test
    public void replaceVariableWithNoVariable() {
        variableStorage.put("id", "4");
        Connector connector = new Connector(new VariableAccessor(), variableStorage);
        assertThat(replaceVariables("cases", connector)).isEqualTo("cases");
    }

    @BeforeEach
    private void reset() {
        ServiceUtil.configurationSource = new ConfigurationSource() {
        };
    }
}
