package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;

import java.util.UUID;

import static com.thoughtworks.gauge.datastore.DataStoreFactory.getScenarioDataStore;

/**
 * The class {@link Value} contains step implementations to create unique or specific values for variables, that are
 * later used in the scenario.
 *
 * For unique values the default lentgh is <pre>8</pre>
 */
public class Value extends Service {
    @Step("Create <variable> with length <length>")
    public void createUniqueId(String variable, int length) {
        String unique = UUID.randomUUID().toString().substring(0, length);
        getScenarioDataStore().put(variable, unique);
    }

    @Step("Create <variable>")
    public void createUniqueId(String variable) {
        createUniqueId(variable, 8);
    }

    @Step("Set <variable> to <value>")
    public void setVariable(String variable, Object value) {
        getScenarioDataStore().put(variable, value);
    }
}
