package ru.practicum.shareit.exception;

public class AlreadyExistException extends ErrorsMapException {
    public AlreadyExistException(String uniqueFieldsNames, String description) {
        super(uniqueFieldsNames, description);
    }
}
