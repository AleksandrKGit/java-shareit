package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class ControllerErrorHandlerTest {
    @Autowired
    private ControllerErrorHandler controllerErrorHandler;

    @Test
    void handleException_shouldReturnMapOfErrorsWithOneElement() {
        Exception exception = new Exception("Exception message");

        Map<String, String> target = controllerErrorHandler.handleException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleBadRequestException_shouldReturnMapOfErrorsWithOneElement() {
        BadRequestException exception = new BadRequestException("field", "error");

        Map<String, String> target = controllerErrorHandler.handleBadRequestException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleAlreadyExistException_shouldReturnMapOfErrorsWithOneElement() {
        AlreadyExistException exception = new AlreadyExistException("field", "error");

        Map<String, String> target = controllerErrorHandler.handleAlreadyExistException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleAccessDeniedException_shouldReturnMapOfErrorsWithOneElement() {
        AccessDeniedException exception = new AccessDeniedException("field", "error");

        Map<String, String> target = controllerErrorHandler.handleAccessDeniedException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleNotFoundException_shouldReturnMapOfErrorsWithOneElement() {
        NotFoundException exception = new NotFoundException("field", "error");

        Map<String, String> target = controllerErrorHandler.handleNotFoundException(exception);

        assertThat(target, aMapWithSize(1));
    }
}