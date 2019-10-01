package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.ServiceUtil;
import com.thoughtworks.gauge.BeforeSuite;

public class GaugeRestSetup {
    @BeforeSuite
    public void before() {
        Registry.init(new Connector());
    }
}
