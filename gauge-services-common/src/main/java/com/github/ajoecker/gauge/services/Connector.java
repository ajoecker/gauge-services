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

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Abstraction of a connection to a service. This is the glue to connect and send to a service, e.g. GraphQL or REST
 */
public class Connector {
    public static final String NO_PREFIX = "";
    private final VariableStorage variableStorage;
    private String endpoint;
    private Optional<ExtractableResponse<Response>> previousResponse = Optional.empty();
    private Response response;
    private VariableAccessor variableAccessor;

    public Connector() {
        this(new VariableAccessor(), VariableStorage.create());
    }

    public Connector(VariableAccessor variableAccessor, VariableStorage variableStorage) {
        setVariableAccessor(variableAccessor);
        setEndpoint(variableAccessor.endpoint());
        this.variableStorage = variableStorage;
    }

    public void setVariableAccessor(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
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

    public String extract(String path) {
        return response.then().extract().path(prefix(path));
    }

    /**
     * Sends a post with the given query and variables
     *
     * @param query     the query
     * @param variables the variables
     */
    public void post(String query, String variables) {
        response = post(query, variables, "", startRequest());
        setPreviousResponse();
    }

    /**
     * Sends a get with the given query
     *
     * @param query the query
     * @return the response of the get query
     */
    public final Response get(String query) {
        return get(query, "", startRequest());
    }

    /**
     * Sends a post with the given query and ensures that one is authenticated.
     *
     * @param query        the query
     * @param variables    the variables
     * @param path         the path to post to
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void post(String query, String variables, String path, LoginHandler loginHandler) {
        response = post(query, variables, path, login(loginHandler));
        setPreviousResponse();
    }

    private void setPreviousResponse() {
        setPreviousResponse(response.then().extract());
    }

    public void setPreviousResponse(ExtractableResponse<Response> previousResponse) {
        this.previousResponse = Optional.ofNullable(previousResponse);
    }

    /**
     * Returns the prefix all paths of a responses must start with.
     * <p>
     * Default is an empty string.
     *
     * @return the prefix
     */
    protected String withPrefix() {
        return NO_PREFIX;
    }

    /**
     * Prefixes the path with the prefix ({@link #withPrefix()}) if the path does not already start with that prefix
     *
     * @param dataPath the json path
     * @return json path with guaranteed {@link #withPrefix()} at beginning
     */
    public String prefix(String dataPath) {
        String prefix = withPrefix();
        if (prefix.length() > 0 && !dataPath.startsWith(prefix)) {
            return prefix + dataPath;
        }
        return dataPath;
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource        the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void get(String resource, String parameter, LoginHandler loginHandler) {
        resource = replaceVariables(resource, this);
        response = get(resource, parameter, login(loginHandler));
        setPreviousResponse();
    }

    /**
     * Sends a get with the given query and ensures that one is authenticated.
     *
     * @param resource        the query
     * @param parameter    optional parameters of the query, empty string if non available
     * @param loginHandler the {@link LoginHandler} for authentication
     */
    public void put(String resource, String parameter, LoginHandler loginHandler) {
        resource = replaceVariables(resource, this);
        response = get(resource, parameter, login(loginHandler));
        setPreviousResponse();
    }

    public void deleteWithLogin(String query, String path, LoginHandler loginHandler) {
        RequestSpecification request = login(loginHandler);
        String realQuery = replaceVariables(query, this);
        response = checkDebugPrint(request.delete(checkTrailingSlash(getCompleteEndpoint(path), realQuery)));
        setPreviousResponse();
    }

    private RequestSpecification login(LoginHandler loginHandler) {
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
    private Response post(String query, String variables, String path, RequestSpecification request) {
        String postEndpoint = getCompleteEndpoint(path);
        Object object = bodyFor(query, variables);
        return checkDebugPrint(request.contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(object)
                .when()
                .post(postEndpoint));
    }

    private String getCompleteEndpoint(String path) {
        return checkTrailingSlash(endpoint, path);
    }

    private String checkTrailingSlash(String base, String path) {
        if (!Strings.isNullOrEmpty(path)) {
            return !base.endsWith("/") ? base + "/" + path : base + path;
        }
        return base;
    }

    private Response checkDebugPrint(Response response) {
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
     * @param query     the query
     * @param variables the variables, empty string if no variables available
     * @return the formatted object for the request
     */
    protected Object bodyFor(String query, String variables) {
        return query;
    }

    /**
     * Sends a get with the given query to the given {@link RequestSpecification}
     *
     * @param query   the query
     * @param request the request
     * @return the {@link Response}
     */
    private Response get(String query, String parameters, RequestSpecification request) {
        String queryPath = getCompleteEndpoint(query);
        if (!Strings.isNullOrEmpty(parameters)) {
            queryPath = queryPath + "?" + parameters;
        }
        return checkDebugPrint(request.contentType(ContentType.JSON)
                .when()
                .get(queryPath));
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

    public boolean hasPreviousResponse() {
        return previousResponse.isPresent();
    }

    public Object pathFromPreviousResponse(String variablePath) {
        return previousResponse.map(pR -> pR.path(prefix(variablePath))).orElseGet(() -> "");
    }

    public void assertResponse(String path, Matcher<?> matcher) {
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

    private boolean matches(Map<Object, Object> target, List<String> keyValues) {
        Iterator<String> iterator = keyValues.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = iterator.next();
            if (!target.get(key).equals(replaceVariables(value, this))) {
                return false;
            }
        }
        return true;
    }

    public Object getFromVariableStorage(String toLookFor) {
        return variableStorage.get(toLookFor);
    }
}
