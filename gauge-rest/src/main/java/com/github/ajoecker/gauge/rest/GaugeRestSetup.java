package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;

public class GaugeRestSetup {
    @BeforeSuite
    public void before() {
        Registry.init(new RestConnector());
    }
}
