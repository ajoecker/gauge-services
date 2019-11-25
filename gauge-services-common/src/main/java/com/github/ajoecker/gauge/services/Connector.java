package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.common.Sender;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
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
    protected final Sender theSender;
    private final VariableStorage variableStorage;
    private ValidatableResponse response;

    public Connector() {
        this(VariableStorage.create(), new Sender(new VariableAccessor()));
    }

    public Connector(VariableStorage variableStorage, Sender sender) {
        this.variableStorage = variableStorage;
        this.theSender = sender;
    }

    public Sender requestSender() {
        return theSender;
    }

    public void setResponse(Response response) {
        this.response = response.then();
    }

    /**
     * Sends a post with the given query
     *
     * @param query the query
     */
    public void post(String query) {
        post(query, "", null);
    }

    /**
     * Returns the prefix for the given data path, as some services will require a certain structure in the response,
     * like graphql's <code>data</code>
     *
     * @param dataPath the path in the response
     * @return the prefixed path. The default implementation returns simply the given path.
     */
    protected String prefix(String dataPath) {
        return dataPath;
    }

    /**
     * Asserts that the given path is empty
     *
     * @param dataPath the path to check
     */
    public void isEmpty(String dataPath) {
        assertResponse(prefix(dataPath), empty());
    }

    /**
     * Sends a post with the given query and ensures that one is logged in, if required.
     *
     * @param query                 the query
     * @param path                  the resource the post is send to
     * @param authenticationHandler the {@link AuthenticationHandler} to ensure authentication
     */
    public void post(String query, String path, AuthenticationHandler authenticationHandler) {
        String postEndpoint = theSender.getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        Response response = theSender.sendPost(authenticationHandler, postEndpoint, object);
        setResponse(response);
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

    /**
     * Returns the value of the given path from the latest response if existing.
     *
     * @param variablePath the path to look for
     * @return the found value
     */
    public Optional<Object> fromLatestResponse(String variablePath) {
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

    /**
     * Extracts the value of the of the given <code>variable</code> from the latest response, where the given
     * <code>attributeValue</code> is matching.
     * <p>
     * Like <code>extract 'id' from 'customer' where 'email=john.doe@gmail.com'</code>
     *
     * @param variable
     * @param parent
     * @param attributeValue
     */
    public void extract(String variable, String parent, String attributeValue) {
        List<String> keyValueList = splitIntoKeyValueList(attributeValue);
        Optional<Object> optionalParent = fromLatestResponse(parent);

        optionalParent.filter(List.class::isInstance).ifPresent(enclosing -> {
            List<Map<Object, Object>> theList = (List<Map<Object, Object>>) enclosing;
            Optional<Map<Object, Object>> first = theList.stream().filter(map -> matches(map, keyValueList)).findFirst();
            first.ifPresent(f -> variableStorage.put(variable, f.get(variable)));
        });
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
     * <p>
     * In case the replaced value contains <code>"</code>, these get masks <code>\"</code>
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

    public Optional<Object> getFromVariableStorage(String toLookFor) {
        return variableStorage.get(toLookFor);
    }
}
