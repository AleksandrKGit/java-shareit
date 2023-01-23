package ru.practicum.shareit.exception;

public class NotFoundException extends ValidationException {
    public NotFoundException(String uniqueFieldsNames, String description) {
        super(uniqueFieldsNames, description);
    }
}