package com.github.ajoecker.gauge.services;

import com.github.ajoecker.gauge.random.data.VariableStorage;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Strings;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    private static final Pattern compile = Pattern.compile("(%.+?%)");
    private static final String MASK = "%";
    private final VariableStorage variableStorage;
    private String endpoint;
    private Optional<ExtractableResponse<Response>> previousResponse = Optional.empty();
    protected Response response;
    private VariableAccessor variableAccessor;

    public Connector() {
        this(new VariableAccessor(), VariableStorage.create());
    }

    public Connector(VariableAccessor variableAccessor, VariableStorage variableStorage) {
        this.variableAccessor = variableAccessor;
        setEndpoint(variableAccessor.endpoint());
        this.variableStorage = variableStorage;
    }

    public VariableAccessor getVariableAccessor() {
        return variableAccessor;
    }

    /**
     * Sets the endpoint, the service is querying
     *
     * @param endpoint the endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Sends a post with the given query and variables
     *
     * @param query the query
     */
    public void post(String query) {
        response = post(query, "", startRequest());
        setPreviousResponse();
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param path         the path to post to
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void post(String query, String path, LoginHandler loginHandler) {
        response = post(query, path, login(loginHandler));
        setPreviousResponse();
    }

    protected void setPreviousResponse() {
        this.previousResponse = Optional.ofNullable(response.then().extract());
    }

    protected String prefix(String dataPath) {
        return dataPath;
    }

    public void isEmpty(String dataPath) {
        assertResponse(prefix(dataPath), empty());
    }

    protected RequestSpecification login(LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        loginHandler.setLogin(request);
        return request;
    }

    /**
     * Sends a post with the given query to the given {@link RequestSpecification}.
     * <p>
     * It also asserts that the status code is 200.
     *
     * @param query   the query
     * @param request the request
     * @return the {@link Response}
     */
    private Response post(String query, String path, RequestSpecification request) {
        String postEndpoint = getCompleteEndpoint(replaceVariables(path));
        Object object = bodyFor(replaceVariables(query));
        return checkDebugPrint(request
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(object)
                .when()
                .post(postEndpoint)
        );
    }

    protected String getCompleteEndpoint(String path) {
        return checkTrailingSlash(endpoint, path);
    }

    protected String checkTrailingSlash(String base, String path) {
        if (!Strings.isNullOrEmpty(path)) {
            return !base.endsWith("/") ? base + "/" + path : base + path;
        }
        return base;
    }

    protected Response checkDebugPrint(Response response) {
        if (variableAccessor.logAll()) {
            response.then().log().all();
        } else if (variableAccessor.logFailure()) {
            response.then().log().ifValidationFails();
        }
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

    private RequestSpecification startRequest() {
        RequestSpecification request = given();
        if (variableAccessor.logAll()) {
            request.when().log().all();
        } else if (variableAccessor.logFailure()) {
            request.when().log().ifValidationFails();
        }
        return request;
    }

    public void verifyStatusCode(int expected) {
        response.then().statusCode(is(expected));
    }

    public Object pathFromPreviousResponse(String variablePath) {
        return previousResponse.map(pR -> pR.path(prefix(variablePath))).orElse("");
    }

    private void assertResponse(String path, Matcher<?> matcher) {
        response.then().assertThat().body(prefix(path), matcher);
    }

    public void clear() {
        previousResponse = Optional.empty();
    }

    public Consumer<Object[]> thenContains(String dataPath) {
        return items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, Matchers.hasItems(items));
            } else {
                assertResponse(dataPath, Matchers.containsString((String) items[0]));
            }
        };
    }

    public Consumer<Object[]> thenIs(String dataPath) {
        return items -> {
            if (response.then().extract().path(prefix(dataPath)) instanceof List) {
                assertResponse(dataPath, containsInAnyOrder(items));
            } else {
                assertResponse(dataPath, is(items[0]));
            }
        };
    }

    public void verifyRequestInLessThan(long timeout) {
        response.then().time(Matchers.lessThanOrEqualTo(timeout));
    }

    public void extract(String variable, String parent, String attributeValue) {
        List<String> keyValueList = splitIntoKeyValueList(attributeValue);
        Object path = response.then().extract().path(parent);
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
            if (!target.get(key).equals(query)) {
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
     * @param v         the string with variables
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
        Object saved = getFromVariableStorage(variable);
        if (saved != null) {
            return saved.toString();
        }
        String prev = pathFromPreviousResponse(variable).toString();
        if (!"".equals(prev)) {
            return prev;
        }
        return variable;
    }

    public Object getFromVariableStorage(String toLookFor) {
        return variableStorage.get(toLookFor);
    }
}
