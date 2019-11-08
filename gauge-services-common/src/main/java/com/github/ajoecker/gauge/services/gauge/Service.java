package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.*;
import com.github.ajoecker.gauge.services.login.LoginHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The class provides the implementation of the gauge specs to validate different queries.
 */
public abstract class Service {
    protected final Connector connector;
    protected final LoginHandler loginHandler;

    public Service() {
        this.connector = Registry.getConnector();
        this.loginHandler = Registry.getLoginHandler();
    }
}
