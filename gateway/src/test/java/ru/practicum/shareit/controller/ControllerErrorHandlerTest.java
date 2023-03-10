package ru.practicum.shareit.controller;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.practicum.shareit.exception.ValidationException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

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
    void handleMissingServletRequestParameterException_shouldReturnMapOfErrorsWithOneElement() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException("name", "type");

        Map<String, String> target = controllerErrorHandler.handleMissingServletRequestParameterException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleHttpMessageNotReadableException_shouldReturnMapOfErrorsWithOneElement() {
        Exception exception = new Exception("Exception message");

        Map<String, String> target = controllerErrorHandler.handleHttpMessageNotReadableException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleHttpMediaTypeNotSupportedException_shouldReturnMapOfErrorsWithOneElement() {
        HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException("text");

        Map<String, String> target = controllerErrorHandler.handleHttpMediaTypeNotSupportedException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleMissingRequestHeaderException_shouldReturnMapOfErrorsWithOneElement() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException("header",
                mock(MethodParameter.class));

        Map<String, String> target = controllerErrorHandler.handleMissingRequestHeaderException(exception);

        assertThat(target, aMapWithSize(1));
    }

    private ConstraintViolation<Object> createConstraintViolation(String path, String message) {
        return ConstraintViolationImpl.forParameterValidation(message, null, null,
                message, Object.class, new Object(), null, null, PathImpl.createPathFromString(path),
                null, null, null);
    }

    @Test
    void handleConstraintViolationException_shouldReturnSortedMapOfErrorsWithKeyFieldsAndValueStringOfSortedErrors() {
        Map<String, String> errors = new TreeMap<>(String::compareTo);
        errors.putAll(Map.of("field1", "error1: null; error2: null", "field2", "error1: null"));
        ConstraintViolationException exception = new ConstraintViolationException("",
                Set.of(createConstraintViolation("field1", "error1"),
                        createConstraintViolation("field1", "error2"),
                        createConstraintViolation("field2", "error1")));

        Map<String, String> target = controllerErrorHandler.handleConstraintViolationException(exception);

        assertThat(target, equalTo(errors));
    }

    @Test
    void handleMethodArgumentNotValidException_shouldReturnSortedMapOfErrorsWithKeyFieldAndValueStringOfSortedErrors() {
        Map<String, String> errors = new TreeMap<>(String::compareTo);
        errors.putAll(Map.of("field1", "error1: null; error2: null", "field2", "error1: null"));
        BindingResult bindingResult = new DirectFieldBindingResult(new Object(), "name");
        bindingResult.addError(new FieldError("name1", "field1", "error1"));
        bindingResult.addError(new FieldError("name2", "field1", "error2"));
        bindingResult.addError(new FieldError("name3", "field2", "error1"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(mock(MethodParameter.class),
                bindingResult);

        Map<String, String> target = controllerErrorHandler.handleMethodArgumentNotValidException(exception);

        assertThat(target, equalTo(errors));
    }

    @Test
    void handleValidationException_shouldReturnMapOfErrorsWithOneElement() {
        ValidationException exception = new ValidationException("field", "error");

        Map<String, String> target = controllerErrorHandler.handleValidationException(exception);

        assertThat(target, aMapWithSize(1));
    }

    @Test
    void handleNoHandlerFoundException_shouldReturnMapOfErrorsWithOneElement() {
        NoHandlerFoundException exception = new NoHandlerFoundException("httpMethod", "requestUrl", HttpHeaders.EMPTY);

        Map<String, String> target = controllerErrorHandler.handleNoHandlerFoundException(exception);

        assertThat(target, aMapWithSize(1));
    }
}