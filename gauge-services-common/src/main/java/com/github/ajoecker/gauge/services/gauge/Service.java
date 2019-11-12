package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.*;
import com.github.ajoecker.gauge.services.login.LoginHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An abstract base class for all different kind of service endpoints, such as POST, GET or DELETE.
 */
public abstract class Service {
    protected final Connector connector;
    protected final LoginHandler loginHandler;

    public Service() {
        this.connector = Registry.getConnector();
        this.loginHandler = Registry.getLoginHandler();
    }
}
