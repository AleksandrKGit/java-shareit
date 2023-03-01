package ru.practicum.shareit.exception;

public class NotFoundException extends ErrorsMapException {
    public NotFoundException(String uniqueFieldsNames, String description) {
        super(uniqueFieldsNames, description);
    }
}