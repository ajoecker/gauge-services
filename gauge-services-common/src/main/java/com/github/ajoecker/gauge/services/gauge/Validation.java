package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.*;
import static com.thoughtworks.gauge.datastore.DataStoreFactory.getScenarioDataStore;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;

public class Validation extends Service {
    @Step({"Then <path> contains <value>", "And <path> contains <value>"})
    public void thenContains(String dataPath, Object value) {
        compare(value, connector.thenContains(dataPath));
    }

    @Step({"Then <path> is <value>", "And <path> is <value>",
            "Then <path> are <value>", "And <path> are <value>"})
    public void thenIs(String dataPath, Object value) {
        Object extractedCacheValue = getScenarioDataStore().get(dataPath);
        if (extractedCacheValue != null) {
            assertThat(extractedCacheValue.toString()).isEqualTo(value);
        } else {
            compare(value, connector.thenIs(dataPath));
        }
    }


    @Step({"Then <dataPath> is empty", "And <dataPath> is empty"})
    public void thenEmpty(String dataPath) {
        connector.assertResponse(connector.prefix(dataPath), empty());
    }

    private void compare(Object value, Consumer<Object[]> match) {
        if (value instanceof String) {
            compareStringValue((String) value, match);
        } else if (value instanceof Table) {
            List<Map<String, String>> expected = ((Table) value).getTableRows().stream().map(ServiceUtil::fromTable).collect(Collectors.toList());
            match.accept(expected.toArray(new Map[expected.size()]));
        }
    }

    private void compareStringValue(String value, Consumer<Object[]> match) {
        String stringValue = value;
        if (isMap(stringValue)) {
            List<Map<String, String>> expected = parseMap(stringValue);
            match.accept(expected.toArray(new Map[expected.size()]));
        } else {
            List<String> expected = Arrays.asList(split(stringValue));
            match.accept(expected.toArray(new String[expected.size()]));
        }
    }
}
