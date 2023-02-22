package ru.practicum.shareit.request;

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
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.DtoFactory.createItemRequestDtoFromClient;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestDtoFromClientTest {
    @Autowired
    LocalValidatorFactoryBean validator;

    @Autowired
    ObjectMapper objectMapper;

    static final String validDescription = "requestDescription";

    @Test
    void validate_withCorrectDescription_shouldReturnEmptyListOfConstraintViolations() {
        ItemRequestDtoFromClient dto = createItemRequestDtoFromClient(validDescription);

        Set<ConstraintViolation<ItemRequestDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(empty()));
    }

    private static Stream<Arguments> incorrectDescription() {
        return Stream.of(
                Arguments.of("Creating with null description", null),
                Arguments.of("creating with big description", Strings.repeat("n", 256)),
                Arguments.of("creating with empty description", ""),
                Arguments.of("Creating with blank description", " \t\n\r   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectDescription")
    void validate_withIncorrectDescription_shouldReturnNotEmptyListOfConstraintViolations(String testName,
                                                                                          String description) {
        ItemRequestDtoFromClient dto = createItemRequestDtoFromClient(description);

        Set<ConstraintViolation<ItemRequestDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(not(empty())));
    }

    @Test
    void fromJson_withNotEmptyDescription_shouldReturnDtoWithNotEmptyDescription() throws JsonProcessingException {
        ItemRequestDtoFromClient target = objectMapper.readValue("{\"description\":\""
                        + validDescription + "\"}", ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", equalTo(validDescription)));
    }

    @Test
    void fromJson_withEmptyDescription_shouldReturnDtoWithEmptyDescription() throws JsonProcessingException {
        ItemRequestDtoFromClient target = objectMapper.readValue("{\"description\":\"\"}",
                ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", is(emptyString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"description\":null}"})
    void fromJson_withNoOrNullDescription_shouldReturnDtoWithNullDescription(String source)
            throws JsonProcessingException {
        ItemRequestDtoFromClient target = objectMapper.readValue(source, ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", is(nullValue())));
    }
}