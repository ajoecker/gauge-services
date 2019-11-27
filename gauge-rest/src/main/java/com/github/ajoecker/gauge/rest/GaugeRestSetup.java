package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;
import com.thoughtworks.gauge.Step;
import org.tinylog.Logger;

public class GaugeRestSetup {
    static final String REST_TYPE = "rest";

    @BeforeSuite
    public void before() {
        Logger.info("setting up REST service");
        Registry.get().init(REST_TYPE, sender -> new RestConnector(sender));
    }

    @Step("Given the rest endpoint <endpoint>")
    public void restEndpoint(String endpoint) {
        Registry registry = Registry.get();
        registry.sender().setEndpoint(endpoint);
        registry.setActiveType(REST_TYPE);
    }
}
