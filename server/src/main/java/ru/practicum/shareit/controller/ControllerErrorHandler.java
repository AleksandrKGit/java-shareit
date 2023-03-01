package ru.practicum.shareit.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import java.util.*;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerErrorHandler {
    private final DefaultLocaleMessageSource messageSource;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleException(Exception ex) {
        log.warn(messageSource.get("controller.serverError") + ": " + ex.getClass() + " " + ex.getMessage());
        return Map.of("error", messageSource.get("controller.serverError"));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleBadRequestException(BadRequestException ex) {
        log.warn(messageSource.get("controller.validationError") + ": " + ex.getErrors());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Map<String, String> handleAlreadyExistException(AlreadyExistException ex) {
        log.warn(messageSource.get("controller.alreadyExist") + ": " + ex.getErrors());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn(messageSource.get("controller.accessDenied") + ": " + ex.getErrors());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        log.warn(messageSource.get("controller.sourceNotFound") + ": " + ex.getErrors());
        return ex.getErrors();
    }
}