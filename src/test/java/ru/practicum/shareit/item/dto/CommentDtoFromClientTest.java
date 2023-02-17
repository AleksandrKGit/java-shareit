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
import ru.practicum.shareit.tools.configurations.AppTestConfiguration;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static ru.practicum.shareit.tools.factories.ItemFactory.createCommentDtoFromClient;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentDtoFromClientTest {
    @Autowired
    LocalValidatorFactoryBean validator;

    @Autowired
    ObjectMapper objectMapper;

    static final String validText = "commentText";

    @Test
    void validate_withCorrectText_shouldReturnEmptyListOfConstraintViolations() {
        CommentDtoFromClient dto = createCommentDtoFromClient(validText);

        Set<ConstraintViolation<CommentDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(empty()));
    }

    private static Stream<Arguments> incorrectText() {
        return Stream.of(
                Arguments.of("Creating with null text", null),
                Arguments.of("creating with big text", Strings.repeat("n", 2001)),
                Arguments.of("creating with empty text", ""),
                Arguments.of("Creating with blank text", " \t\n\r   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectText")
    void validate_withIncorrectText_shouldReturnNotEmptyListOfConstraintViolations(String testName,
                                                                                          String text) {
        CommentDtoFromClient dto = createCommentDtoFromClient(text);

        Set<ConstraintViolation<CommentDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(not(empty())));
    }

    @Test
    void fromJson_withNotEmptyText_shouldReturnDtoWithNotEmptyText() throws JsonProcessingException {
        CommentDtoFromClient target = objectMapper.readValue("{\"text\":\"" + validText + "\"}",
                CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", equalTo(validText)));
    }

    @Test
    void fromJson_withEmptyText_shouldReturnDtoWithEmptyText() throws JsonProcessingException {
        CommentDtoFromClient target = objectMapper.readValue("{\"text\":\"\"}",
                CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", is(emptyString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"text\":null}"})
    void fromJson_withNoOrNullText_shouldReturnDtoWithNullText(String source)
            throws JsonProcessingException {
        CommentDtoFromClient target = objectMapper.readValue(source, CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", is(nullValue())));
    }
}