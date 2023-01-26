package ru.practicum.shareit.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.AlreadyExistException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import java.util.Map;
import java.util.TreeMap;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ControllerErrorHandler {
    private final DefaultLocaleMessageSource messageSource;

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, String> handleException(Exception ex) {
        log.warn(messageSource.get("controller.serverError") + ": " + ex.getClass() + " " + ex.getMessage());
        return Map.of("serverError", messageSource.get("controller.serverError"));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleHttpMessageNotReadableException(Exception ex) {
        log.warn(messageSource.get("controller.incorrectDataFormat") + ": " + ex.toString());
        return Map.of("requestError", messageSource.get("controller.incorrectDataFormat"));
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.warn(messageSource.get("controller.messingHeader") + ": " + ex.getHeaderName());
        return Map.of("requestError", messageSource.get("controller.messingHeader") + ": " + ex.getHeaderName());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new TreeMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn(messageSource.get("controller.validationError") + ": " + errors);
        return errors;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidationException(ValidationException ex) {
        log.warn(messageSource.get("controller.validationError") + ": " + ex.getMessage());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Map<String, String> handleAlreadyExistException(AlreadyExistException ex) {
        log.warn(messageSource.get("controller.alreadyExist") + ": " + ex.getMessage());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn(messageSource.get("controller.accessDenied") + ": " + ex.getMessage());
        return ex.getErrors();
    }

    @ExceptionHandler()
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleNotFoundException(NotFoundException ex) {
        log.warn(messageSource.get("controller.sourceNotFound") + ": " + ex.getMessage());
        return ex.getErrors();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn(messageSource.get("controller.sourceNotFound") + ": " + ex.getMessage());
        return Map.of("notFoundError", messageSource.get("controller.sourceNotFound"));
    }
}
