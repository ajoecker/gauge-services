package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.gauge.Service;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariablesInQuery;

/**
 * The class {@link PUT} contains all steps for a POST request
 */
public class PUT extends Service {
    @Step({"When putting <query>", "And putting <query>"})
    public void putting(String query) {
        connector.put(query, "", "", loginHandler);
    }

    @Step({"When putting <query> to <path>", "And putting <query> to <path>"})
    public void putting(String query, String path) {
        connector.put(query, "", path, loginHandler);
    }

    @Step({"When putting <query> with <parameters>", "And putting <query> with <parameters>"})
    public void puttingWithParameters(String query, Object parameters) {
        puttingWithVariables(query, "", parameters);
    }

    @Step({"When putting <query> to <path> with <variables>", "And putting <query> to <path> with <variables>"})
    public void puttingWithVariables(String query, String path, Object variables) {
        if (variables instanceof String) {
            String variablesAsString = (String) variables;
            if (variablesAsString.trim().startsWith("{") && variablesAsString.trim().endsWith("}")) {
                connector.put(query, variablesAsString, path, loginHandler);
            } else {
                connector.put(replaceVariablesInQuery(query, variablesAsString, connector), "", path, loginHandler);
            }
        } else if (variables instanceof Table) {
            connector.put(replaceVariablesInQuery(query, (Table) variables, connector), "", path, loginHandler);
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }
}
