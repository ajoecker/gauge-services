package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariablesInQuery;

/**
 * The class {@link PUT} contains all steps for a POST request
 */
public class PUT extends Service {
    @Step({"When putting <query>", "And putting <query>"})
    public void putting(String query) {
        putWithVariables(query, "", "");
    }

    @Step({"When putting <query> to <path>", "And putting <query> to <path>"})
    public void putting(String query, String path) {
        putWithVariables(query, path, "");
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
                putWithVariables(query, path, variablesAsString);
            } else {
                putWithVariables(replaceVariablesInQuery(query, variablesAsString, connector), path, "");
            }
        } else if (variables instanceof Table) {
            putWithVariables(replaceVariablesInQuery(query, (Table) variables, connector), path, "");
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }

    private void putWithVariables(String query, String path, String variables) {
        connector.put(query, variables, path, loginHandler);
    }
}
