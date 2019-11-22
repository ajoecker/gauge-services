package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.common.RequestSender;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Strings;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    private static final Pattern compile = Pattern.compile("(%.+?%)");
    private static final String MASK = "%";
    protected final RequestSender requestSender;
    private final VariableStorage variableStorage;
    private ValidatableResponse response;

    public Connector() {
        this(VariableStorage.create(), new RequestSender(new VariableAccessor()));
    }

    public Connector(VariableStorage variableStorage, RequestSender requestSender) {
        this.variableStorage = variableStorage;
        this.requestSender = requestSender;
    }

    public RequestSender requestSender() {
        return requestSender;
    }

    public void setResponse(Response response) {
        this.response = response.then();
    }

    /**
     * Sends a post with the given query and variables
     *
     * @param query the query
     */
    public void post(String query) {
        post(query, "", null);
    }

    protected String prefix(String dataPath) {
        return dataPath;
    }

    public void isEmpty(String dataPath) {
        assertResponse(prefix(dataPath), empty());
    }

    /**
     * Sends a post with the given query to the given {@link RequestSpecification}.
     * <p>
     * It also asserts that the status code is 200.
     *
     * @param query the query
     * @return the {@link Response}
     */
    public Response post(String query, String path, LoginHandler loginHandler) {
        String postEndpoint = requestSender.getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        Response response = requestSender.sendPost(loginHandler, postEndpoint, object);
        setResponse(response);
        return response;
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

    public void verifyStatusCode(int expected) {
        response.statusCode(is(expected));
    }

    public Optional<Object> pathFromPreviousResponse(String variablePath) {
        return Optional.ofNullable(response.extract().path(prefix(variablePath)));
    }

    private void assertResponse(String path, Matcher<?> matcher) {
        response.assertThat().body(prefix(path), matcher);
    }

    public Consumer<Object[]> thenContains(String dataPath) {
        return items -> {
            if (response.extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, Matchers.hasItems(items));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) items[0]));
            }
        };
    }

    public Consumer<Object[]> thenIs(String dataPath) {
        return items -> {
            if (response.extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        };
    }

    public void verifyRequestInLessThan(long timeout) {
        response.time(Matchers.lessThanOrEqualTo(timeout));
    }

    public void extract(String variable, String parent, String attributeValue) {
        List<String> keyValueList = splitIntoKeyValueList(attributeValue);
        Object path = response.extract().path(parent);
        if (path instanceof List) {
            List<Map<Object, Object>> theList = (List<Map<Object, Object>>) path;
            Optional<Map<Object, Object>> first = theList.stream().filter(map -> matches(map, keyValueList)).findFirst();
            first.ifPresent(f -> variableStorage.put(variable, f.get(variable)));
        }
    }

    private List<String> splitIntoKeyValueList(String s) {
        return Arrays.stream(s.split("\\s*,\\s*"))
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
     *
     * @param v the string with variables
     * @return a replaced string with no variables
     */
    protected String replaceVariables(String v) {
        java.util.regex.Matcher matcher = compile.matcher(v);
        String result = v;
        while (matcher.find()) {
            String variableValue = getVariableValue(matcher.group(1).replace(MASK, "").trim());
            String substring = v.substring(matcher.start(1), matcher.end(1));
            result = result.replace(substring, variableValue);
        }
        return result;
    }

    private String getVariableValue(String variable) {
        return getFromVariableStorage(variable).map(Object::toString)
                .orElseGet(() ->
                        pathFromPreviousResponse(variable)
                                .map(Object::toString)
                                .filter(s -> s.length() > 0)
                                .orElse(variable));
    }

    public Optional<Object> getFromVariableStorage(String toLookFor) {
        return variableStorage.get(toLookFor);
    }
}
