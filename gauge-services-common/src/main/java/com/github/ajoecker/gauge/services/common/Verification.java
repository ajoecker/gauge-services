package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.*;
import org.tinylog.Logger;

import java.util.ArrayList;
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

    @ContinueOnFailure
    @Step({"Then <path> contains <value>", "And <path> contains <value>"})
    public void thenContains(String dataPath, Object value) {
        compare(value, connector().thenContains(dataPath));
    }

    @ContinueOnFailure
    @Step({"Then <path> start with <value>", "And <path> start with <value>"})
    public void startWith(String dataPath, Object value) {
        compare(value, connector().startWith(dataPath));
    }

    @ContinueOnFailure
    @Step({"Then <path> is <value>", "And <path> is <value>", "Then <path> are <value>", "And <path> are <value>"})
    public void thenIs(String dataPath, Object value) {
        connector().getFromVariableStorage(dataPath).
                ifPresentOrElse(v -> assertThat(v.toString()).isEqualTo(value),
                        () -> compare(value, connector().thenIs(dataPath)));
    }

    @ContinueOnFailure
    @Step({"Then <actual> is identical to <expected>", "And <actual> is identical to <expected>"})
    public void compareVariables(String actual, String allExpected) {
        Object actualValue = connector().getFromVariableStorage(actual).orElseThrow();
        List<String> errorMessages = new ArrayList<>();

        stream(split(allExpected)).forEach(each -> connector().getFromVariableStorage(each.trim()).ifPresentOrElse(s -> {
            if (!s.equals(actualValue)) {
                errorMessages.add(each + " (" + s + ") is not equal to " + actual + " (" + actualValue + ")");
            }
            else {
                Logger.info("value '{}' ({}) is identical to '{}' ({})", actual, actualValue, each, s);
            }
        }, () -> errorMessages.add(each + " is not known")));

        assertThat(errorMessages).withFailMessage("\n" + errorMessages.stream().collect(Collectors.joining("\n"))).isEmpty();
    }

    @ContinueOnFailure
    @Step({"Then <inJson> from json <toJson> is <value>", "And <inJson> from json <toJson> is <value>"})
    public void jsonExtractionEqual(String pathInJson, String pathtoJson, Object value) {
        connector().extractFromJson(pathInJson, pathtoJson, pathInJson);
        thenIs(pathInJson, value);
    }

    @ContinueOnFailure
    @Step({"Then <dataPath> is empty", "And <dataPath> is empty"})
    public void thenEmpty(String dataPath) {
        connector().assertResponse(dataPath, empty());
    }

    @ContinueOnFailure
    @Step({"Then <dataPath> is not empty", "And <dataPath> is not empty"})
    public void thenNotEmpty(String dataPath) {
        connector().assertResponse(dataPath, not(empty()));
    }

    @ContinueOnFailure
    @Step({"Then <dataPath> is true", "And <dataPath> is true"})
    public void thenTrue(String dataPath) {
        connector().assertResponse(dataPath, is(true));
    }

    @ContinueOnFailure
    @Step({"Then the response is equal to <content>", "And the response is equal to <content>"})
    public void thenIsEqual(String content) {
        connector().assertResponseAsJson(content);
    }

    @ContinueOnFailure
    @Step({"Then <dataPath> is false", "And <dataPath> is false"})
    public void thenFalse(String dataPath) {
        connector().assertResponse(dataPath, is(false));
    }

    private void compare(Object value, Consumer<Object[]> match) {
        if (value instanceof String) {
            compareStringValue((String) value, match);
        } else if (value instanceof Table) {
            List<String> value1 = ((Table) value).getColumnValues("value");
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
