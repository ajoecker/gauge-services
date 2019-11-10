package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariablesInQuery;

public class POST extends Service {
    @Step({"When posting <query>", "And posting <query>"})
    public void posting(String query) {
        postWithVariables(query, "", "");
    }

    @Step({"When posting <query> to <path>", "And posting <query> to <path>"})
    public void posting(String query, String path) {
        postWithVariables(query, path, "");
    }

    @Step({"When posting <query> with <variables>", "And posting <query> with <variables>"})
    public void postingWithVariables(String query, Object variables) {
        postingWithVariables(query, "", variables);
    }

    @Step({"When posting <query> to <path> with <variables>", "And posting <query> to <path> with <variables>"})
    public void postingWithVariables(String query, String path, Object variables) {
        if (variables instanceof String) {
            String variablesAsString = (String) variables;
            if (variablesAsString.trim().startsWith("{") && variablesAsString.trim().endsWith("}")) {
                postWithVariables(query, path, variablesAsString);
            } else {
                postWithVariables(replaceVariablesInQuery(query, variablesAsString, connector), path, "");
            }
        } else if (variables instanceof Table) {
            postWithVariables(replaceVariablesInQuery(query, (Table) variables, connector), path, "");
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }

    private void postWithVariables(String query, String path, String variables) {
        connector.post(query, variables, path, loginHandler);
    }
}
