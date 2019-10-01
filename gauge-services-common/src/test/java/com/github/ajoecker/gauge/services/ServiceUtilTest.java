package com.github.ajoecker.gauge.services;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.ajoecker.gauge.services.ServiceUtil.isMap;

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
        Assertions.assertEquals(parse, List.of(Map.of("name", "Banksy", "nationality", "British")));
    }

    @Test
    public void parsesMultiMap() {
        String mapString = "{name: Banksy, nationality: British}, {name: Pablo Picasso, nationality: Spanish}";
        List<Map<String, String>> parse = ServiceUtil.parseMap(mapString);
        Assertions.assertEquals(parse, List.of(
                Map.of("name", "Banksy", "nationality", "British"),
                Map.of("name", "Pablo Picasso", "nationality", "Spanish")
        ));
    }

    @Test
    public void replaceVariablesInQueryWorks() {
        String query = "{\n" +
                "    popular_artists(size: $size) {\n" +
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
        Assertions.assertEquals(ServiceUtil.replaceVariablesInQuery(query, "size:2", Optional.empty(), null), queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithOwnFormat() {
        String query = "{\n" +
                "    popular_artists(size: ##size) {\n" +
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
        Assertions.assertEquals(ServiceUtil.replaceVariablesInQuery(query, "size:2", Optional.empty(), null), queryReplaced);
    }

    @Test
    public void replaceVariablesInQueryWithVariablesWorks() {
        String query = "{\n" +
                "    popular_artists(size: $size) {\n" +
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

        Assertions.assertEquals(ServiceUtil.replaceVariablesInQuery(query, "size:$foo", Optional.of(re), new Connector() {
            @Override
            public Response post(String query, RequestSpecification request) {
                return null;
            }
        }), queryReplaced);
    }

    @BeforeEach
    private void reset() {
        ServiceUtil.configurationSource = new ConfigurationSource() {
        };
    }
}
