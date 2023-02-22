package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.DtoFactory.createBookingDtoFromClient;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingDtoFromClientTest {
    @Autowired
    LocalValidatorFactoryBean validator;

    @Autowired
    ObjectMapper objectMapper;

    static final LocalDateTime validStart = LocalDateTime.now().plusMinutes(1);
    static final LocalDateTime validEnd = validStart.plusMinutes(1);
    static final Long validItemId = 1L;

    @Test
    void validate_withCorrectFields_shouldReturnEmptyListOfConstraintViolations() {
        BookingDtoFromClient dto = createBookingDtoFromClient(validItemId, validStart, validEnd);

        Set<ConstraintViolation<BookingDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(empty()));
    }

    private static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("Creating with null itemId",
                        null, validStart, validEnd),

                Arguments.of("Creating with null start",
                        validItemId, null, validEnd),

                Arguments.of("Creating with start in past",
                        validItemId, LocalDateTime.now().minusDays(1), validEnd),

                Arguments.of("creating with null end",
                        validItemId, validStart, null),

                Arguments.of("creating with end in past",
                        validItemId, validStart, LocalDateTime.now().minusDays(1))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFields")
    void validate_withInvalidFields_shouldReturnNotEmptyListOfConstraintViolations(
            String testName, Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDtoFromClient dto = createBookingDtoFromClient(itemId, start, end);

        Set<ConstraintViolation<BookingDtoFromClient>> target = validator.validate(dto);

        assertThat(target, is(not(empty())));
    }

    @Test
    void fromJson_withNotNullFields_shouldReturnDtoWithNotNullFields() throws JsonProcessingException {
        BookingDtoFromClient target = objectMapper.readValue("{\"itemId\":\"" + validItemId + "\", "
                        + "\"start\":\"" + validStart + "\", \"end\":\"" + validEnd + "\"}",
                BookingDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("itemId", equalTo(validItemId)),
                hasProperty("start", equalTo(validStart)),
                hasProperty("end", equalTo(validEnd))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"itemId\":null, \"start\":null, \"end\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source)
            throws JsonProcessingException {
        BookingDtoFromClient target = objectMapper.readValue(source, BookingDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("itemId", is(nullValue())),
                hasProperty("start", is(nullValue())),
                hasProperty("end", is(nullValue()))
        ));
    }
}