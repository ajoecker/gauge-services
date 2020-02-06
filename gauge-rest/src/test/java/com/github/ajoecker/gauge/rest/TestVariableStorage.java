package com.github.ajoecker.gauge.rest;

import com.github.ajoecker.gauge.random.data.VariableStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestVariableStorage implements VariableStorage {
    Map<String, Object> storage = new HashMap<>();

    @Override
    public void put(String key, Object value) {
        storage.put(key, value);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public void print() {
        System.out.println(storage);
    }
}
