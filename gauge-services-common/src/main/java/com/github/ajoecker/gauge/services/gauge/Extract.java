package com.github.ajoecker.gauge.services.gauge;

import com.thoughtworks.gauge.Step;

public class Extract extends Service {
    @Step({"Then extracting <variable> from <parent> where <attributevalue>",
            "And extracting <path> from <parent> where <attributevalue>"})
    public void extractPathWithParent(String variable, String parent, String attributeValue) {
        connector.extract(variable, parent, attributeValue);
    }

    @Step({"Then extracting <variable> where <attributevalue>",
            "And extracting <path> where <attributevalue>"})
    public void extractPath(String variable, String attributeValue) {
        connector.extract(variable, "", attributeValue);
    }

}
