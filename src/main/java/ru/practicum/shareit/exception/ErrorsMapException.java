package ru.practicum.shareit.exception;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ErrorsMapException extends RuntimeException {
    private final Map<String, String> errors;

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    public ErrorsMapException(Map<String, String> errors) {
        super();
        this.errors = new TreeMap<>(String::compareToIgnoreCase);
        this.errors.putAll(errors);
    }

    public ErrorsMapException(String key, String value) {
        this(Map.of(key, value));
    }

    @Override
    public String getMessage() {
        return errors.toString();
    }
}
