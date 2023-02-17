package ru.practicum.shareit.exception;

public class ValidationException extends ErrorsMapException {
    public ValidationException(String fieldName, String errorMessage) {
        super(fieldName, errorMessage);
    }
}
