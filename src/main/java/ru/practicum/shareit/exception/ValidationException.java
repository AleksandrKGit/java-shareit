package ru.practicum.shareit.exception;

import lombok.Getter;
import java.util.Map;
import java.util.TreeMap;

public class ValidationException extends RuntimeException {
    @Getter
    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super();
        this.errors = new TreeMap<>(errors);
    }

    public ValidationException(String fieldName, String errorMessage) {
        this(Map.of(fieldName, errorMessage));
    }

    @Override
    public String getMessage() {
        return errors.toString();
    }
}
