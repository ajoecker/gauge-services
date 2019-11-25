package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.common.Service;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;

import java.util.stream.Collectors;

/**
 * The class {@link GET} contains all step implementations for a GET request.
 */
public class GET extends Service<RestConnector> {
    @Step({"When getting <resource>", "And getting <resource>"})
    public void get(String resource) {
        connector.get(resource, "", authenticationHandler);
    }

    @Step({"When getting <query> with <parameters>", "And getting <query> with <parameters>"})
    public void gettingParameters(String query, Object parameters) {
        if (parameters instanceof Table) {
            getForGaugeTable(query, (Table) parameters);
        } else if (parameters instanceof String) {
            // the string is already in the format var1=val,var2=val2 - so only , must be replaced with &
            connector.get(query, ((String) parameters).replaceAll("\\s+", "").replace(',', '&'), authenticationHandler);
        }
    }

    // transforms the gauge table in a set of parameters like var1=val1&var2=val2
    private void getForGaugeTable(String query, Table table) {
        String getParameters = table.getTableRows().stream()
                .map(tableRow -> tableRow.getCell("name") + "=" + tableRow.getCell("value"))
                .collect(Collectors.joining("&"));
        connector.get(query, getParameters, authenticationHandler);
    }
}
