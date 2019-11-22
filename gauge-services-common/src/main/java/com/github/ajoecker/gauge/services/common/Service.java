package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.login.LoginHandler;

/**
 * An abstract base class for all different kind of service endpoints, such as POST, GET or DELETE.
 */
public abstract class Service<T extends Connector> {
    protected final T connector;
    protected final LoginHandler loginHandler;

    public Service() {
        this.connector = (T) Registry.getConnector();
        this.loginHandler = Registry.getLoginHandler();
    }
}
