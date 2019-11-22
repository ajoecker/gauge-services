package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.VariableAccessor;
import com.github.ajoecker.gauge.services.login.LoginHandler;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RequestSender {
    private VariableAccessor variableAccessor;
    private String endpoint;

    public RequestSender(VariableAccessor variableAccessor) {
        this.variableAccessor = variableAccessor;
        this.endpoint = variableAccessor.endpoint();
    }

    protected RequestSpecification login(LoginHandler loginHandler) {
        RequestSpecification request = startRequest();
        if (loginHandler != null) {
            loginHandler.setLogin(request);
        }
        return request;
    }

    public Response sendDelete(LoginHandler loginHandler, String deletePath) {
        return checkDebugPrint(login(loginHandler).delete(deletePath));
    }

    public Response sendGet(LoginHandler loginHandler, String queryPath) {
        return send(rs -> rs.get(queryPath), loginHandler);
    }

    private Response send(Function<RequestSpecification, Response> call, Object body, LoginHandler loginHandler) {
        return checkDebugPrint(call.apply(initRequest(loginHandler).body(body).when()));
    }

    public Response sendPut(LoginHandler loginHandler, String theEndpoint, Object object) {
        return send(rs -> rs.put(theEndpoint), object, loginHandler);
    }

    public Response sendPost(LoginHandler loginHandler, String endpoint, Object body) {
        return send(rs -> rs.post(endpoint), body, loginHandler);
    }

    private Response send(Function<RequestSpecification, Response> call, LoginHandler loginHandler) {
        return checkDebugPrint(call.apply(initRequest(loginHandler).when()));
    }

    private RequestSpecification initRequest(LoginHandler loginHandler) {
        return login(loginHandler).contentType(ContentType.JSON).accept(ContentType.JSON);
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
