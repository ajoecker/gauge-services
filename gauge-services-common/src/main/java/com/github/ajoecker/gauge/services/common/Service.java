package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;

/**
 * An abstract base class for all different kind of service endpoints, such as POST, GET or DELETE.
 */
public abstract class Service<T extends Connector> {
    protected final T connector;
    protected final AuthenticationHandler authenticationHandler;

    public Service() {
        this.connector = (T) Registry.getConnector();
        this.authenticationHandler = Registry.getAuthenticationHandler();
    }
}
