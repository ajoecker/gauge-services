package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralTest {
    @Test
    public void statusCodeIsChecked() {
        final int code = 200;
        Connector connector = new Connector() {
            @Override
            public void verifyStatusCode(int expected) {
                assertEquals(code, expected);
            }
        };
        Registry.init(connector);
        new General().verifyStatusCode(code);
    }
}
