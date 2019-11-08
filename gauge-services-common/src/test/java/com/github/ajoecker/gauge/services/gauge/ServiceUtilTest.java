package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.ConfigurationSource;
import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.gauge.ServiceUtil;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.isMap;
import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.splitIntoKeyValueList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceUtilTest {
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
    public void replaceVariablesInQueryWithOwnFormat() {
        String query = "{\n" +
                "    popular_artists(size: ##size##) {\n" +
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
        ServiceUtil.configurationSource = new ConfigurationSource() {
            @Override
            public String variableMask() {
                return "##";
            }
        };
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

    @BeforeEach
    private void reset() {
        ServiceUtil.configurationSource = new ConfigurationSource() {
        };
    }
}
