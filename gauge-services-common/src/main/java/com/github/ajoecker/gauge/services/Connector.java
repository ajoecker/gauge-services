package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thoughtworks.gauge.Table;
import io.restassured.path.json.JsonPath;
import org.hamcrest.*;
import org.tinylog.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    private static final Pattern compile = Pattern.compile("(%.+?%)");
    private static final String MASK = "%";
    private final VariableStorage variableStorage;
    private final String prefix;
    protected final Sender sender;

    public Connector(Sender sender) {
        this(VariableStorage.get(), sender, "");
    }

    public Connector(VariableStorage variableStorage, Sender sender) {
        this(variableStorage, sender, "");
    }

    public Connector(Sender sender, String prefix) {
        this(VariableStorage.get(), sender, prefix);
    }

    public Connector(VariableStorage variableStorage, Sender sender, String prefix) {
        this.variableStorage = variableStorage;
        this.sender = sender;
        this.prefix = prefix;
    }

    /**
     * Sends a post with the given query
     *
     * @param query the query
     */
    public final void post(String query) {
        post(query, "", null);
    }

    private String prefixfy(String path) {
        if (!"".equals(prefix) && !path.startsWith(prefix)) {
            return prefix + path;
        }
        return path;
    }

    /**
     * Sends a post with the given query and ensures that one is logged in, if required.
     *
     * @param query                 the query
     * @param path                  the resource the post is send to
     * @param authenticationHandler the {@link AuthenticationHandler} to ensure authentication
     */
    public final void post(String query, String path, AuthenticationHandler authenticationHandler) {
        post(query, path, authenticationHandler, Function.identity());
    }

    public void post(String query, String path, Table table, AuthenticationHandler authenticationHandler) {
        post(query, path, authenticationHandler, v -> replaceVariablesFromTable(v, table));
    }

    public void put(String query, String path, Table table, AuthenticationHandler authenticationHandler) {
        put(query, path, authenticationHandler, v -> replaceVariablesFromTable(v, table));
    }

    private void put(String query, String path, AuthenticationHandler authenticationHandler, Function<String, String> queryMaker) {
        String postEndpoint = sender.getCompleteEndpoint(replaceVariables(path));
        Logger.info("posting to " + postEndpoint);
        Object object = bodyFor(replaceVariables(queryMaker.apply(query)));
        sender.setResponse(sender.sendPut(authenticationHandler, postEndpoint, object));
        Logger.info("posting done");
    }

    private void post(String query, String path, AuthenticationHandler authenticationHandler, Function<String, String> queryMaker) {
        String postEndpoint = sender.getCompleteEndpoint(replaceVariables(path));
        Logger.info("posting to " + postEndpoint);
        Object object = bodyFor(replaceVariables(queryMaker.apply(query)));
        sender.setResponse(sender.sendPost(authenticationHandler, postEndpoint, object));
        Logger.info("posting done");
    }

    /**
     * Allows to modify the query if the body of the request requires to format the query differently.
     * <p>
     * Default method simply returns the query and applies no changes
     *
     * @param query the query
     * @return the formatted object for the request
     */
    protected Object bodyFor(String query) {
        return query;
    }

    /**
     * Returns the value of the given path from the latest response if existing.
     *
     * @param variablePath the path to look for
     * @return the found value
     */
    public Optional<Object> fromLatestResponse(String variablePath) {
        Optional<Object> value = Optional.ofNullable(sender.path(prefixfy(variablePath)));
        Logger.info("retrieving {} from latest response: {}", variablePath, value);
        return value;
    }

    /**
     * Asserts the given path in the response with the given {@link Matcher}
     *
     * @param path    the path to the wanted value
     * @param matcher the matcher to verify the path
     */
    public void assertResponse(String path, Matcher<?> matcher) {
        sender.assertResponse(prefixfy(path), matcher);
    }

    public Consumer<Object[]> startWith(String dataPath) {
        return actual -> {
            if (sender.path(prefixfy(dataPath)) instanceof List) {
                assertResponse(dataPath, new ListStartWith(actual));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) actual[0]));
            }
        };
    }

    public Consumer<Object[]> thenContains(String dataPath) {
        return items -> {
            if (sender.path(prefixfy(dataPath)) instanceof List) {
                assertResponse(dataPath, Matchers.hasItems(items));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) items[0]));
            }
        };
    }

    public Consumer<Object[]> thenIs(String dataPath) {
        return items -> {
            if (sender.path(prefixfy(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        };
    }

    /**
     * Extracts the value of the of the given <code>variable</code> from the latest response, where the given
     * <code>attributeValue</code> is matching.
     * <p>
     * Like <code>extract 'id' from 'customer' where 'email=john.doe@gmail.com'</code>
     *
     * @param variable       the variable to extract
     * @param parent         the parent from where to find the variable or empty string for root
     * @param attributeValue the list of key=values that must match the entry in which the variable can be found.
     *                       This can be null, then the first (or if not a list, the only one) is taken.
     */
    public final void extract(String variable, String parent, String attributeValue, String saver) {
        Logger.info("extracting variable {} where {} is", variable, attributeValue);
        List<String> keyValueList = splitIntoKeyValueList(attributeValue);
        Optional<Object> optionalParent = fromLatestResponse(parent);

        if (keyValueList.isEmpty()) {
            optionalParent.ifPresent(o -> storeVariableFromMap(variable, (Map<Object, Object>) o, saver));
        } else {
            optionalParent.filter(List.class::isInstance).ifPresent(enclosing -> storeMatchInMap(variable, keyValueList, enclosing, saver));
        }
    }

    private void storeMatchInMap(String variable, List<String> keyValueList, Object enclosing, String saver) {
        Logger.info("extracting from found value {}", enclosing);
        List<Map<Object, Object>> theList = (List<Map<Object, Object>>) enclosing;
        Optional<Map<Object, Object>> match = theList.stream().filter(map -> matches(map, keyValueList)).findFirst();
        match.ifPresent(keyValue -> {
            Object value = keyValue.get(variable);
            Logger.info("extraction successful for {}", value);
            variableStorage.put(saver, value);
        });
    }

    private void storeVariableFromMap(String variable, Map<Object, Object> map, String saver) {
        Object value = getValue(variable, map);
        Logger.info("extraction successful for {} from {}", value, variable);
        variableStorage.put(saver, value);
    }

    private Object getValue(String variable, Map<Object, Object> map) {
        String[] split = variable.split("\\.");
        for (String level : split) {
            Object o = map.get(level);
            if (o instanceof Map) {
                map = (Map<Object, Object>) o;
            } else {
                return o;
            }
        }
        throw new IllegalArgumentException("no value found for " + variable + " in " + map);
    }

    private List<String> splitIntoKeyValueList(String s) {
        return s.length() == 0 ? List.of() : Arrays.stream(s.split("\\s*,\\s*"))
                .flatMap(s1 -> Arrays.stream(s1.split("=")))
                .collect(Collectors.toList());
    }

    private boolean matches(Map<Object, Object> target, List<String> keyValues) {
        Iterator<String> iterator = keyValues.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String query = replaceVariables(iterator.next());
            if (!query.equals(target.get(key))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Replaces all variables from the given string by
     *
     * <ol>
     *     <li>Checking the value from {@link com.github.ajoecker.gauge.random.data.VariableStorage}</li>
     *     <li>Checking the value from a previous response</li>
     * </ol>
     * <p>
     * In case the replaced value contains <code>"</code>, these get masks <code>\"</code>
     *
     * @param v the string with variables
     * @return a replaced string with no variables
     */
    protected final String replaceVariables(String v) {
        return replaceVariables(v, this::getVariableValue);
    }

    private final String replaceVariables(String v, Function<String, Optional<Object>> retriever) {
        java.util.regex.Matcher matcher = compile.matcher(v);
        String result = v;
        while (matcher.find()) {
            Optional<Object> value = retriever.apply(matcher.group(1).replace(MASK, "").trim());
            String variableValue = value.map(Object::toString).orElseThrow();
            String substring = v.substring(matcher.start(1), matcher.end(1));
            result = result.replace(substring, variableValue.replace("\"", "\\\""));
        }
        return result;
    }

    public static void main(String[] args) {
        String s = "{\n" +
                "  \"data\": {\n" +
                "    \"contract\": {\n" +
                "      \"id\": 44950,\n" +
                "      \"annualPrice\": \"423,72 EUR\",\n" +
                "      \"inResignationPeriod\": false,\n" +
                "      \"monthlyRate\": \"35,31\",\n" +
                "      \"status\": \"policed\",\n" +
                "      \"insuranceConfirmationNumbers\": [\n" +
                "        {\n" +
                "          \"daysLeft\": 11,\n" +
                "          \"id\": \"4441\",\n" +
                "          \"number\": \"NXFSWZS\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"startOfInsurance\": \"2020-01-01\",\n" +
                "      \"premium\": \"423,72\",\n" +
                "      \"price\": \"423,72 EUR\",\n" +
                "      \"mutations\": {\n" +
                "        \"items\": [\n" +
                "          {\n" +
                "            \"validAt\": \"2020-01-01\",\n" +
                "            \"id\": 16507,\n" +
                "            \"mutableChanges\": \"{\\\"attributes\\\":{\\\"product_datum_attributes\\\":{\\\"max_km_per_year\\\":20000}}}\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"validAt\": \"2020-01-01\",\n" +
                "            \"id\": 16510,\n" +
                "            \"mutableChanges\": \"{\\\"attributes\\\":{\\\"product_datum_attributes\\\":{\\\"max_km_per_year\\\":15000}}}\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"validAt\": \"2020-01-01\",\n" +
                "            \"id\": 16513,\n" +
                "            \"mutableChanges\": \"{\\\"attributes\\\":{\\\"product_datum_attributes\\\":{\\\"max_km_per_year\\\":20000}}}\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"validAt\": \"2020-01-01\",\n" +
                "            \"id\": 16516,\n" +
                "            \"mutableChanges\": \"{\\\"attributes\\\":{\\\"product_datum_attributes\\\":{\\\"max_km_per_year\\\":15000}}}\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        JsonPath from = JsonPath.from(s);
        Object o = from.get("data.contract.mutations.items.find{ it.id == 16516 }.mutableChanges");
        System.out.println(o);
    }

    private String replaceVariablesFromTable(String query, Table table) {
        return replaceVariables(query, v -> foor(table, v).or(() -> getVariableValue(v)));
    }

    private Optional<Object> foor(Table table, String variable) {
        return table.getTableRows().stream().
                filter(tableRow -> {
                    String variable1 = tableRow.getCell("variable");
                    Logger.info("{} == {} : {}", variable, variable1, variable.equals(variable1));
                    return variable1.equals(variable);
                }).
                findFirst().map(tableRow -> tableRow.getCell("value"));
    }

    private Optional<Object> getVariableValue(String variable) {
        return getFromVariableStorage(variable).or(() -> fromLatestResponse(variable));
    }

    public final Optional<Object> getFromVariableStorage(String toLookFor) {
        return variableStorage.get(toLookFor);
    }

    public void assertResponseAsJson(String content) {
        JsonParser jsonParser = new JsonParser();
        JsonElement actualJson = jsonParser.parse(sender.responseAsJson());
        JsonElement expectedJson = jsonParser.parse(content);
        assertThat(actualJson).isEqualTo(expectedJson);
    }

    public void extractSum(String variable, String variablesToSum) {
        BigDecimal reduce = stream(variablesToSum.split(","))
                .map(each -> getVariableValue(each.trim()).orElseGet(getDefault(each)))
                .flatMap(o -> cast(o).stream())
                .reduce(new BigDecimal(0), BigDecimal::add);
        double value = reduce.doubleValue();
        Logger.info("saving {} as sum {}", variable, value);
        variableStorage.put(variable, value);
    }

    private Supplier<Object> getDefault(String each) {
        return () -> {
            Logger.warn("variable '{}' can not be found - 0 is used !", each);
            return 0;
        };
    }

    private Optional<BigDecimal> cast(Object retrievedValue) {
        if (retrievedValue instanceof Double) {
            return Optional.of(BigDecimal.valueOf((Double) retrievedValue));
        } else if (retrievedValue instanceof Integer) {
            return Optional.of(BigDecimal.valueOf((Integer) retrievedValue));
        } else if (retrievedValue instanceof Float) {
            return Optional.of(new BigDecimal(Float.toString((Float) retrievedValue)));
        } else if (retrievedValue instanceof String) {
            String val = (String) retrievedValue;
            if (!val.isEmpty()) {
                return Optional.of(new BigDecimal(val));
            } else {
                Logger.warn("retrieved value is empty string");
                return Optional.empty();
            }
        }
        Logger.warn("{} with class {} is not supported for sum", retrievedValue, retrievedValue.getClass());
        return Optional.empty();
    }

    public Object extractFromJson(String pathInJson, String pathToJson, String variableToStore) {
        Object value = JsonPath.from(sender.path(prefixfy(pathToJson)).toString()).get(pathInJson);
        Logger.info("extracted {} from json {} in path {}", value, pathToJson, pathInJson);
        if (!variableToStore.equals("")) {
            saveValue(value, theValue -> variableStorage.put(variableToStore, theValue));
        }
        return value;
    }

    private void saveValue(Object value, Consumer<Object> saver) {
        NumberFormat instance = NumberFormat.getInstance(Locale.getDefault());
        try {
            Number number = instance.parse(value.toString());
            double doubleValue = number.doubleValue();
            Logger.info("saving {} as double", doubleValue);
            saver.accept(doubleValue);
        } catch (ParseException e) {
            Logger.info("saving {} as object", value);
            saver.accept(value);
        }
    }

    private static class ListStartWith extends BaseMatcher<Object> {
        private final Object[] actual;

        public ListStartWith(Object[] actual) {
            this.actual = actual;
        }

        @Override
        public void describeTo(Description description) {
            // not sure
        }

        private String getValue(Object toLook) {
            if (toLook instanceof Map) {
                // we only allow on column values here
                return ((Map) toLook).values().iterator().next().toString();
            }
            return toLook.toString();
        }

        @Override
        public boolean matches(Object o) {
            Logger.info("try to match {} against {}", o, Arrays.toString(actual));
            List<String> asList = ((List<String>) o);
            return stream(actual).allMatch(partial ->
                    asList.stream().anyMatch(o2 -> o2.startsWith(getValue(partial)))
            );
        }

        @Override
        public void describeMismatch(Object o, Description description) {
            description.appendText("was not found").appendValue(o);
        }
    }
}
