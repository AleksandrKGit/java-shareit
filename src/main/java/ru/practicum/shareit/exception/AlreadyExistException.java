package ru.practicum.shareit.exception;

public class AlreadyExistException extends ValidationException {
    public AlreadyExistException(String uniqueFieldsNames, String description) {
        super(uniqueFieldsNames, description);
    }
}
