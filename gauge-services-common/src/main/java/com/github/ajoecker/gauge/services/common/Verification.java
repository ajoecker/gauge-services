package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.TableCell;
import com.thoughtworks.gauge.TableRow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

public final class Verification extends Service<Connector> {
    private static final String COMMA_SEPARATED = "\\s*,\\s*";

    @Step({"Then <path> contains <value>", "And <path> contains <value>"})
    public void thenContains(String dataPath, Object value) {
        compare(value, connector.thenContains(dataPath));
    }

    @Step({"Then <path> is <value>", "And <path> is <value>",
            "Then <path> are <value>", "And <path> are <value>"})
    public void thenIs(String dataPath, Object value) {
        connector.getFromVariableStorage(dataPath).
                ifPresentOrElse(v -> assertThat(v.toString()).isEqualTo(value), () -> compare(value, connector.thenIs(dataPath)));
    }

    @Step({"Then <dataPath> is empty", "And <dataPath> is empty"})
    public void thenEmpty(String dataPath) {
        connector.assertResponse(dataPath, empty());
    }

    @Step({"Then <dataPath> is not empty", "And <dataPath> is not empty"})
    public void thenNotEmpty(String dataPath) {
        connector.assertResponse(dataPath, not(empty()));
    }

    @Step({"Then <dataPath> is true", "And <dataPath> is true"})
    public void thenTrue(String dataPath) {
        connector.assertResponse(dataPath, is(true));
    }

    @Step({"Then the response is equal to <content>", "And the response is equal to <content>"})
    public void thenIsEqual(String content) {
        connector.assertResponseAsJson(content);
    }

    @Step({"Then <dataPath> is false", "And <dataPath> is false"})
    public void thenFalse(String dataPath) {
        connector.assertResponse(dataPath, is(false));
    }

    private void compare(Object value, Consumer<Object[]> match) {
        if (value instanceof String) {
            compareStringValue((String) value, match);
        } else if (value instanceof Table) {
            List<Map<String, String>> expected = ((Table) value).getTableRows().stream().map(this::fromTable).collect(Collectors.toList());
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

    private List<Map<String, String>> parseMap(String value) {
        String[] values = value.trim().split("}" + COMMA_SEPARATED);
        return stream(values).map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, String> toMap(String full) {
        String prepared = full.replace("{", "").replace("}", "");
        return stream(prepared.split(COMMA_SEPARATED))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
    }

    private boolean isMap(String value) {
        return value.contains("{") && value.contains("}");
    }

    private Map<String, String> fromTable(TableRow tableRow) {
        return tableRow.getTableCells().stream().collect(Collectors.toMap(TableCell::getColumnName, TableCell::getValue));
    }

    private static String[] split(String stringValue) {
        return stringValue.trim().split(COMMA_SEPARATED);
    }
}
