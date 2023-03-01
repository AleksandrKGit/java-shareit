package ru.practicum.shareit.exception;

public class BadRequestException extends ErrorsMapException {
    public BadRequestException(String fieldName, String errorMessage) {
        super(fieldName, errorMessage);
    }
}