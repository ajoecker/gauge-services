package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.Sender;
import com.github.ajoecker.gauge.services.login.AuthenticationHandler;

/**
 * An abstract base class for all different kind of service endpoints, such as POST, GET or DELETE.
 */
public abstract class Service<T extends Connector> {
    protected final Sender sender;
    protected final AuthenticationHandler authenticationHandler;

    public Service() {
        Registry registry = Registry.get();
        this.sender = registry.sender();
        this.authenticationHandler = registry.getAuthenticationHandler();
    }

    protected final T connector() {
        return (T) Registry.get().connector();
    }
}
