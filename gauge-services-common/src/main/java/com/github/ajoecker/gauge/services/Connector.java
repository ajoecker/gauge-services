package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.restassured.path.json.JsonPath;
import org.hamcrest.*;
import org.tinylog.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.math.DoubleMath.isMathematicalInteger;
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
    protected final Sender sender;
    private final VariableStorage variableStorage;
    private final String prefix;

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
        String postEndpoint = sender.getCompleteEndpoint(replaceVariables(path));
        Logger.info("posting to " + postEndpoint);
        Object object = bodyFor(replaceVariables(query));
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
                // assertResponse(dataPath, Matchers.hasItems(actual));
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
    public final void extract(String variable, String parent, String attributeValue) {
        Logger.info("extracting variable {} where {} is", variable, attributeValue);
        List<String> keyValueList = splitIntoKeyValueList(attributeValue);
        Optional<Object> optionalParent = fromLatestResponse(parent);

        if (keyValueList.isEmpty()) {
            optionalParent.ifPresent(o -> storeVariableFromMap(variable, (Map<Object, Object>) o));
        } else {
            optionalParent.filter(List.class::isInstance).ifPresent(enclosing -> storeMatchInMap(variable, keyValueList, enclosing));
        }
    }

    private void storeMatchInMap(String variable, List<String> keyValueList, Object enclosing) {
        Logger.info("extracting from found value {}", enclosing);
        List<Map<Object, Object>> theList = (List<Map<Object, Object>>) enclosing;
        Optional<Map<Object, Object>> match = theList.stream().filter(map -> matches(map, keyValueList)).findFirst();
        match.ifPresent(keyValue -> {
            Object value = keyValue.get(variable);
            Logger.info("extraction successful for {}", value);
            variableStorage.put(variable, value);
        });
    }

    private void storeVariableFromMap(String variable, Map<Object, Object> o) {
        Map<Object, Object> asMap = o;
        Object value = asMap.get(variable);
        Logger.info("extraction successful for {} from {}", value, variable);
        variableStorage.put(variable, value);
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
        java.util.regex.Matcher matcher = compile.matcher(v);
        String result = v;
        while (matcher.find()) {
            String variableValue = getVariableValue(matcher.group(1).replace(MASK, "").trim());
            String substring = v.substring(matcher.start(1), matcher.end(1));
            result = result.replace(substring, variableValue.replace("\"", "\\\""));
        }
        return result;
    }

    private String getVariableValue(String variable) {
        return getFromVariableStorage(variable).map(Object::toString)
                .orElseGet(() ->
                        fromLatestResponse(variable)
                                .map(Object::toString)
                                .filter(s -> s.length() > 0)
                                .orElse(variable));
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
        stream(variablesToSum.split(","))
                .map(String::trim)
                .map(this::getVariableValue)
                .map(BigDecimal::new)
                .reduce(BigDecimal::add)
                .map(BigDecimal::doubleValue)
                .ifPresent(aDouble -> {
                    Logger.info("saving {} as sum {}", variable, aDouble);
                    variableStorage.put(variable, aDouble);
                });
    }

    public void extractFromJson(String pathInJson, String pathToJson, String variableToStore) {
        Object value = JsonPath.from(sender.path(prefixfy(pathToJson)).toString()).get(pathInJson);
        Logger.info("extracted {} from json {} in path {}", value, pathToJson, pathInJson);
        saveValue(value, theValue -> variableStorage.put(variableToStore, theValue));
    }

    public static void main(String[] args) {
        double d = 634.0799999999999;
        System.out.println(Math.floor(d));
        System.out.println(Math.ceil(d));
    }

    private void saveValue(Object value, Consumer<Object> saver) {
        NumberFormat instance = NumberFormat.getInstance(Locale.getDefault());
        try {
            Number number = instance.parse(value.toString());
            double doubleValue = number.doubleValue();
            Logger.info("saving {} as double", doubleValue);
            saver.accept(doubleValue);
        } catch (ParseException e) {
            e.printStackTrace();
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

        private boolean inList(Object toLook, List<String> o) {
            return o.stream()
                    .filter(o2 -> o2.startsWith(getValue(toLook)))
                    .findAny()
                    .isPresent();
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
            return stream(actual).allMatch(partial -> inList(partial, (List<String>) o));
        }

        @Override
        public void describeMismatch(Object o, Description description) {
            description.appendText("was not found").appendValue(o);
        }
    }
}
