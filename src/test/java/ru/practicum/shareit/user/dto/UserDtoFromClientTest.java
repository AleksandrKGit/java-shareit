package ru.practicum.shareit.user.dto;

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
import ru.practicum.shareit.tools.configurations.AppTestConfiguration;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.ConstraintViolation;
import java.util.*;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.practicum.shareit.tools.factories.UserFactory.*;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserDtoFromClientTest {
    @Autowired
    LocalValidatorFactoryBean validator;

    @Autowired
    ObjectMapper objectMapper;

    static final String validName = "userName";
    static final String validEmail = "user@email.com";

    private static Stream<Arguments> validDto() {
        return Stream.of(
                Arguments.of("creating with valid fields",
                        createUserDtoFromClient(validName, validEmail), OnCreate.class),

                Arguments.of("update with valid fields",
                        createUserDtoFromClient(validName, validEmail), OnUpdate.class),

                Arguments.of("update with null fields",
                        createUserDtoFromClient(null, null), OnUpdate.class)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validDto")
    void validate_withCorrectFields_shouldReturnEmptyListOfConstraintViolations(String testName,
                                                                                UserDtoFromClient dto, Class<?> group) {
        Set<ConstraintViolation<UserDtoFromClient>> target = validator.validate(dto, group);

        assertThat(target, is(empty()));
    }

    private static Stream<Arguments> invalidDto() {
        String bigName = Strings.repeat("a", 256);
        String blankName = " \t\r\n   ";
        String bigEmail = Strings.repeat("a", 242) + "email@mail.com";
        String invalidEmail = "email@mail@com";

        return Stream.of(
                Arguments.of("creating with null name",
                        createUserDtoFromClient(null, validEmail), OnCreate.class),

                Arguments.of("creating with null email",
                        createUserDtoFromClient(validName, null), OnCreate.class),

                Arguments.of("creating with empty name",
                        createUserDtoFromClient("", validEmail), OnCreate.class),

                Arguments.of("creating with empty email",
                        createUserDtoFromClient(validName, ""), OnCreate.class),

                Arguments.of("creating with big name",
                        createUserDtoFromClient(bigName, validEmail), OnCreate.class),

                Arguments.of("creating with big email",
                        createUserDtoFromClient(validName, bigEmail), OnCreate.class),

                Arguments.of("creating with blank name",
                        createUserDtoFromClient(blankName, validEmail), OnCreate.class),

                Arguments.of("creating with invalid email",
                        createUserDtoFromClient(validName, invalidEmail), OnCreate.class),

                Arguments.of("updating with empty name",
                        createUserDtoFromClient("", validEmail), OnUpdate.class),

                Arguments.of("updating with empty email",
                        createUserDtoFromClient(validName, ""), OnUpdate.class),

                Arguments.of("updating with big name",
                        createUserDtoFromClient(bigName, validEmail), OnUpdate.class),

                Arguments.of("updating with big email",
                        createUserDtoFromClient(validName, bigEmail), OnUpdate.class),

                Arguments.of("updating with blank name",
                        createUserDtoFromClient(blankName, validEmail), OnUpdate.class),

                Arguments.of("updating with invalid email",
                        createUserDtoFromClient(validName, invalidEmail), OnUpdate.class)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDto")
    void validate_withInvalidFields_shouldReturnNotEmptyListOfConstraintViolations(
            String testName, UserDtoFromClient dto, Class<?> group) {
        Set<ConstraintViolation<UserDtoFromClient>> target = validator.validate(dto, group);

        assertThat(target, is(not(empty())));
    }

    @Test
    void fromJson_withNotEmptyFields_shouldReturnDtoWithNotEmptyFields() throws JsonProcessingException {
        UserDtoFromClient target = objectMapper.readValue("{\"name\":\"" + validName + "\", "
                        + "\"email\":\"" + validEmail + "\"}", UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", equalTo(validName)),
                hasProperty("email", equalTo(validEmail))
        ));
    }

    @Test
    void fromJson_withEmptyFields_shouldReturnDtoWithEmptyFields() throws JsonProcessingException {
        UserDtoFromClient target = objectMapper.readValue("{\"name\":\"\", \"email\":\"\"}",
                UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(emptyString())),
                hasProperty("email", is(emptyString()))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":null, \"email\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source) throws JsonProcessingException {
        UserDtoFromClient target = objectMapper.readValue(source, UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(nullValue())),
                hasProperty("email", is(nullValue()))
        ));
    }
}