package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;
import com.google.common.base.Strings;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.function.Function;

import static io.restassured.RestAssured.given;

public class Sender {
    private VariableAccessor variableAccessor;
    private String endpoint;

    public Sender(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
        this.endpoint = variableAccessor.endpoint();
    }

    protected RequestSpecification login(AuthenticationHandler authenticationHandler) {
        RequestSpecification request = startRequest();
        if (authenticationHandler != null) {
            authenticationHandler.setLogin(request);
        }
        return request;
    }

    public Response sendDelete(AuthenticationHandler authenticationHandler, String deletePath) {
        return checkDebugPrint(login(authenticationHandler).delete(deletePath));
    }

    public Response sendGet(AuthenticationHandler authenticationHandler, String queryPath) {
        return send(rs -> rs.get(queryPath), authenticationHandler);
    }

    private Response send(Function<RequestSpecification, Response> call, Object body, AuthenticationHandler authenticationHandler) {
        return checkDebugPrint(call.apply(initRequest(authenticationHandler).body(body).when()));
    }

    public Response sendPut(AuthenticationHandler authenticationHandler, String theEndpoint, Object object) {
        return send(rs -> rs.put(theEndpoint), object, authenticationHandler);
    }

    public Response sendPost(AuthenticationHandler authenticationHandler, String endpoint, Object body) {
        return send(rs -> rs.post(endpoint), body, authenticationHandler);
    }

    private Response send(Function<RequestSpecification, Response> call, AuthenticationHandler authenticationHandler) {
        return checkDebugPrint(call.apply(initRequest(authenticationHandler).when()));
    }

    private RequestSpecification initRequest(AuthenticationHandler authenticationHandler) {
        return login(authenticationHandler).contentType(ContentType.JSON).accept(ContentType.JSON);
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

    private Response checkDebugPrint(Response response) {
        if (variableAccessor.logAll()) {
            response.then().log().all();
        } else if (variableAccessor.logFailure()) {
            response.then().log().ifValidationFails();
        }
        return response;
    }

    public String getCompleteEndpoint(String path) {
        if (Strings.isNullOrEmpty(endpoint)) {
            throw new IllegalStateException("no endpoint is given");
        }
        return checkTrailingSlash(endpoint, path);
    }

    public String checkTrailingSlash(String base, String path) {
        if (!Strings.isNullOrEmpty(path)) {
            return !base.endsWith("/") ? base + "/" + path : base + path;
        }
        return base;
    }

    public VariableAccessor getVariableAccessor() {
        return variableAccessor;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
