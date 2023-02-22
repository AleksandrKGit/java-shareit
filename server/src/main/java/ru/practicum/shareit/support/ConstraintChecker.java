package ru.practicum.shareit.support;

public class ConstraintChecker {
    private static final int numOfInnerCauses = 3;

    public static boolean check(Exception exception, String constraintName) {
        if (constraintName == null || constraintName.isEmpty() || exception == null) {
            return false;
        }

        constraintName = constraintName.toLowerCase();

        Throwable cause = exception;

        for (int i = 0; i < numOfInnerCauses; i++) {
            if (cause.getMessage() != null && cause.getMessage().toLowerCase().contains(constraintName)) {
                return true;
            }

            cause = cause.getCause();

            if (cause == null) {
                break;
            }
        }

        return false;
    }
}