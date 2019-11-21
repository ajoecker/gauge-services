package com.github.ajoecker.gauge.services.gauge;

import com.github.ajoecker.gauge.services.Connector;
import com.thoughtworks.gauge.Step;

/**
 * The class {@link Extract} contains all step implementation for extracting a value from a response.
 */
public class Extract extends Service<Connector> {
    @Step({"Then extracting <variable> from <parent> where <attribute>",
            "And extracting <variable> from <parent> where <attribute>"})
    public void extractPathWithParent(String variable, String parent, String attributeValue) {
        connector.extract(variable, parent, attributeValue);
    }

    @Step({"Then extracting <variable> where <attribute>",
            "And extracting <variable> where <attribute>"})
    public void extractPath(String variable, String attributeValue) {
        connector.extract(variable, "", attributeValue);
    }
}
