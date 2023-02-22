package ru.practicum.shareit.exception;

public class AccessDeniedException extends ErrorsMapException {
    public AccessDeniedException(String user, String object) {
        super(user, object);
    }
}
