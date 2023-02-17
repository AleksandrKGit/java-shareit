package ru.practicum.shareit.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;
import java.util.stream.Collectors;

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
        return Map.of("serverError", messageSource.get("controller.serverError"));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn(messageSource.get("controller.missingRequestParameter") + ": " + ex.getParameterName());
        return Map.of("requestError", messageSource.get("controller.missingRequestParameter") + ": "
                + ex.getParameterName());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleHttpMessageNotReadableException(Exception ex) {
        log.warn(messageSource.get("controller.incorrectDataFormat") + ": " + ex.getClass() + " " + ex.getMessage());
        return Map.of("requestError", messageSource.get("controller.incorrectDataFormat"));
    }

    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    @ResponseBody
    public Map<String, String> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.warn(messageSource.get("controller.incorrectMediaType") + ": " + ex.getContentType());
        return Map.of("requestError", messageSource.get("controller.incorrectMediaType") + ": " +
                ex.getContentType());
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.warn(messageSource.get("controller.missingHeader") + ": " + ex.getHeaderName());
        return Map.of("requestError", messageSource.get("controller.missingHeader") + ": " + ex.getHeaderName());
    }

    private Map<String, String> handleValidationErrors(Map<String, List<String>> fieldsErrors) {
        Map<String, String> errors = new TreeMap<>(String::compareTo);
        errors.putAll(fieldsErrors.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> {
                    Collections.sort(e.getValue());
                    return String.join("; ", e.getValue());
                })));
        log.warn(messageSource.get("controller.validationError") + ": " + errors);
        return errors;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, List<String>> fieldsErrors = new HashMap<>();
        ex.getConstraintViolations().forEach((constraintViolation -> {
            String fieldName = null;
            for (Path.Node node : constraintViolation.getPropertyPath()) {
                fieldName = node.getName();
            }
            if (fieldName != null) {
                String errorMessage = constraintViolation.getMessage() + ": " + constraintViolation.getInvalidValue();
                if (!fieldsErrors.containsKey(fieldName)) {
                    fieldsErrors.put(fieldName, new LinkedList<>());
                }
                fieldsErrors.get(fieldName).add(errorMessage);
            }
        }));
        return handleValidationErrors(fieldsErrors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldsErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage() + ": " + ((FieldError) error).getRejectedValue();
            if (!fieldsErrors.containsKey(fieldName)) {
                fieldsErrors.put(fieldName, new LinkedList<>());
            }
            fieldsErrors.get(fieldName).add(errorMessage);
        });
        return handleValidationErrors(fieldsErrors);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidationException(ValidationException ex) {
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn(messageSource.get("controller.sourceNotFound") + ": " + ex.getMessage());
        return Map.of(messageSource.get("controller.error"), messageSource.get("controller.sourceNotFound"));
    }
}
