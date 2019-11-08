package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import java.util.stream.Collectors;

public class GET extends Service {
    @Step({"When getting <query>", "And getting <query>"})
    public void get(String query) {
        getWithParameters(query, "");
    }

    @Step({"When getting <query> with <variables>", "And getting <query> with <variables>"})
    public void gettingWithVariables(String query, Object parameters) {
        if (parameters instanceof Table) {
            Table table = (Table) parameters;
            String getParameters = table.getTableRows().stream()
                    .map(tableRow -> tableRow.getCell("name") + "=" + tableRow.getCell("value"))
                    .collect(Collectors.joining("&"));
            getWithParameters(query, getParameters);
        } else if (parameters instanceof String) {
            getWithParameters(query, ((String) parameters).replaceAll("\\s+", "").replace(',', '&'));
        }
    }

    private void getWithParameters(String query, String variables) {
        connector.getWithLogin(query, variables, loginHandler);
    }
}
