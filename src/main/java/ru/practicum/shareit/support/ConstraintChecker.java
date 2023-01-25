package ru.practicum.shareit.support;

import org.springframework.dao.DataIntegrityViolationException;

public class ConstraintChecker {
    public static boolean check(DataIntegrityViolationException exception, String constraintName) {
        System.out.println();
        return exception != null && exception.getCause() != null && exception.getCause().getCause() != null
                && exception.getCause().getCause().getMessage() != null
                && exception.getCause().getCause().getMessage().contains(constraintName);
    }
}
