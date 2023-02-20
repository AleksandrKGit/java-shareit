package ru.practicum.shareit.validation.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class NullOrNotBlankValidatorTest {
    private NullOrNotBlankValidator nullOrNotBlankValidator;

    @BeforeEach
    void setUp() {
        nullOrNotBlankValidator = new NullOrNotBlankValidator();
    }

    @Test
    void isValid_withNull_shouldReturnTrue() {
        boolean isValid = nullOrNotBlankValidator.isValid(null, null);

        assertThat(isValid, is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", " \t  a    "})
    void isValid_withStringContainingNotBlankSymbol_shouldReturnTrue(String source) {
        boolean isValid = nullOrNotBlankValidator.isValid(source, null);

        assertThat(isValid, is(true));
    }

    private static Stream<Arguments> emptyOrBlankString() {
        return Stream.of(
                Arguments.of("Empty string", ""),
                Arguments.of("String of spaces", "    "),
                Arguments.of("String of blank symbols", "\r  \r\t\n  ")
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyOrBlankString")
    void isValid_withEmptyOrBlankString_shouldReturnFalse(String testName, String source) {
        boolean isValid = nullOrNotBlankValidator.isValid(source, null);

        assertThat(isValid, is(false));
    }
}