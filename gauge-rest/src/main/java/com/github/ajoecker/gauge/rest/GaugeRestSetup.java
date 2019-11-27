package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Registry;
import com.thoughtworks.gauge.BeforeSuite;
import org.tinylog.Logger;

public class GaugeRestSetup {
    @BeforeSuite
    public void before() {
        Logger.info("setting up REST service");
        Registry.init(new RestConnector());
    }
}
