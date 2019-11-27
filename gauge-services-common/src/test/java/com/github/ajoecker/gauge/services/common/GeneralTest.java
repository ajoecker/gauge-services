package com.github.ajoecker.gauge.services.common;

import com.github.ajoecker.gauge.services.Connector;
import com.github.ajoecker.gauge.services.Registry;
import com.github.ajoecker.gauge.services.Sender;
import com.github.ajoecker.gauge.services.VariableAccessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralTest {
    @Test
    public void statusCodeIsChecked() {
        final int code = 200;
        Sender sender = new Sender(new VariableAccessor()) {
            public void verifyStatusCode(int expected) {
                assertEquals(code, expected);
            }
        };
        Connector connector = new Connector(sender);
        Registry.get().init("foo", sender, connector, null);
        new General().verifyStatusCode(code);
    }
}
