package ru.practicum.shareit.exception;

public class AccessDeniedException extends ValidationException {
    public AccessDeniedException(String user, String object) {
        super(user, object);
    }
}
