package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.DtoFactory.createItemDtoFromClient;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemDtoFromClientTest {
    @Autowired
    LocalValidatorFactoryBean validator;

    @Autowired
    ObjectMapper objectMapper;

    static final String validName = "itemName";

    static final String validDescription = "item@description.com";

    static final Boolean validAvailable = true;

    static final Long validRequestId = 1L;

    private static Stream<Arguments> validDto() {
        return Stream.of(
                Arguments.of("creating with valid fields",
                        createItemDtoFromClient(validName, validDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with null requestId",
                        createItemDtoFromClient(validName, validDescription, validAvailable, null),
                        OnCreate.class),

                Arguments.of("update with valid fields",
                        createItemDtoFromClient(validName, validDescription, validAvailable, null),
                        OnUpdate.class),

                Arguments.of("update with null fields",
                        createItemDtoFromClient(null, null, null, null), OnUpdate.class)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDto")
    void validate_withCorrectFields_shouldReturnEmptyListOfConstraintViolations(String testName,
                                                                                ItemDtoFromClient dto, Class<?> group) {
        Set<ConstraintViolation<ItemDtoFromClient>> target = validator.validate(dto, group);

        assertThat(target, is(empty()));
    }

    private static Stream<Arguments> invalidDto() {
        String bigName = Strings.repeat("a", 256);
        String blankName = " \t\r\n   ";
        String bigDescription = Strings.repeat("a", 2001);

        return Stream.of(
                Arguments.of("creating with null name",
                        createItemDtoFromClient(null, validDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with null description",
                        createItemDtoFromClient(validName, null, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with null available",
                        createItemDtoFromClient(validName, validDescription, null, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with empty name",
                        createItemDtoFromClient("", validDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with big name",
                        createItemDtoFromClient(bigName, validDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with big description",
                        createItemDtoFromClient(validName, bigDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("creating with blank name",
                        createItemDtoFromClient(blankName, validDescription, validAvailable, validRequestId),
                        OnCreate.class),

                Arguments.of("updating with empty name",
                        createItemDtoFromClient("", validDescription, validAvailable, validRequestId),
                        OnUpdate.class),

                Arguments.of("updating with big name",
                        createItemDtoFromClient(bigName, validDescription, validAvailable, validRequestId),
                        OnUpdate.class),

                Arguments.of("updating with big description",
                        createItemDtoFromClient(validName, bigDescription, validAvailable, validRequestId),
                        OnUpdate.class),

                Arguments.of("updating with blank name",
                        createItemDtoFromClient(blankName, validDescription, validAvailable, validRequestId),
                        OnUpdate.class)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDto")
    void validate_withInvalidFields_shouldReturnNotEmptyListOfConstraintViolations(
            String testName, ItemDtoFromClient dto, Class<?> group) {
        Set<ConstraintViolation<ItemDtoFromClient>> target = validator.validate(dto, group);

        assertThat(target, is(not(empty())));
    }

    @Test
    void fromJson_withNotEmptyFields_shouldReturnDtoWithNotEmptyFields() throws JsonProcessingException {
        ItemDtoFromClient target = objectMapper.readValue("{\"name\":\"" + validName + "\", "
                + "\"description\":\"" + validDescription + "\", \"available\":" + validAvailable + ", "
                + "\"requestId\":" + validRequestId + "}", ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", equalTo(validName)),
                hasProperty("description", equalTo(validDescription)),
                hasProperty("available", equalTo(validAvailable)),
                hasProperty("requestId", equalTo(validRequestId))
        ));
    }

    @Test
    void fromJson_withEmptyFields_shouldReturnDtoWithEmptyFields() throws JsonProcessingException {
        ItemDtoFromClient target = objectMapper.readValue("{\"name\":\"\", \"description\":\"\"}",
                ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(emptyString())),
                hasProperty("description", is(emptyString()))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":null, \"description\":null, \"available\":null, \"requestId\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source) throws JsonProcessingException {
        ItemDtoFromClient target = objectMapper.readValue(source, ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }
}