package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link General} contains common step implementations for all different kinds, like checking the status code,
 * the response time etc.
 */
public class General extends Service {
    @Step({"Then the request finished in less than <timeout> ms", "And the request finished in less than <timeout> ms",
            "Then the request finished in less than <timeout>ms", "And the request finished in less than <timeout>ms"})
    public void requestInLessThanMs(long timeout) {
        connector.verifyRequestInLessThan(timeout);
    }

    @Step({"Then the request finished in less than <timeout> s", "And the request finished in less than <timeout> s",
            "Then the request finished in less than <timeout>s", "And the request finished in less than <timeout>s"})
    public void requestInLessThan(long timeout) {
        requestInLessThanMs(timeout * 1000);
    }

    @Step({"Then status code is <code>", "And status code is <code>"})
    public void verifyStatusCode(int expected) {
        connector.verifyStatusCode(expected);
    }

    @Step({"When waiting for <seconds> s", "And waiting for <seconds> s"})
    public void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            // that is fine
        }
    }

    @Step("Given the endpoint <endpoint>")
    public void useEndpoint(String endpoint) {
        connector.setEndpoint(endpoint);
    }

    @AfterScenario
    public void clearResponse() {
        connector.clear();
    }
}
