package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.gauge.Service;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariables;
import static com.github.ajoecker.gauge.services.gauge.ServiceUtil.replaceVariablesInQuery;

public class RestPost extends Service {

    @Step({"When posting <query> to <path> with <variables>", "And posting <query> to <path> with <variables>"})
    public void postingWithVariables(String query, String path, Object variables) {
        if (variables instanceof String) {
            String variablesAsString = (String) variables;
            if (variablesAsString.trim().startsWith("{") && variablesAsString.trim().endsWith("}")) {
                connector.post(query, replaceVariables(variablesAsString, connector), path, loginHandler);
            } else {
                connector.post(replaceVariablesInQuery(query, variablesAsString, connector), "", path, loginHandler);
            }
        } else if (variables instanceof Table) {
            connector.post(replaceVariablesInQuery(query, (Table) variables, connector), "", path, loginHandler);
        } else {
            throw new IllegalArgumentException("unknown variable types " + variables.getClass() + " for " + variables);
        }
    }
}
