package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class ErrorsMapExceptionTest {
    private static Stream<Arguments> withMap() {
        return Stream.of(
                Arguments.of(Map.of("", ""), "{=}"),
                Arguments.of(Map.of("k1", ""), "{k1=}"),
                Arguments.of(Map.of("k1", "v1"), "{k1=v1}"),
                Arguments.of(Map.of("k1", "v1", "k0", ""), "{k0=, k1=v1}"),
                Arguments.of(Map.of("k3", "m3", "k1", "m1", "k4", "m4", "a",
                                "m6"), "{a=m6, k1=m1, k3=m3, k4=m4}")
        );
    }

    @ParameterizedTest
    @MethodSource("withMap")
    void constructor_withErrorsMap_shouldSaveThisMapToSortedUnmodifiableMapOfErrors(Map<String, String> errors,
                                                                                    String message) {
        ErrorsMapException target = new ErrorsMapException(errors);

        assertThat(target, allOf(
                hasProperty("errors", equalTo(errors)),
                hasProperty("message", equalTo(message))
        ));

        assertThrows(UnsupportedOperationException.class, () -> target.getErrors().put("key", "value"));
    }

    private static Stream<Arguments> withKeyValue() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("k1", ""),
                Arguments.of("", "v1"),
                Arguments.of("k1", "v1"),
                Arguments.of("a", "a")
        );
    }

    @ParameterizedTest
    @MethodSource("withKeyValue")
    void constructor_withNotNullKeyAndValue_shouldSaveKeyValueToSortedUnmodifiableMapOfErrors(String key,
                                                                                              String value) {
        ErrorsMapException target = new ErrorsMapException(key, value);
        Map<String, String> expectedErrors = Map.of(key, value);

        assertThat(target, allOf(
                hasProperty("errors", equalTo(expectedErrors)),
                hasProperty("message", equalTo(expectedErrors.toString()))
        ));

        assertThrows(UnsupportedOperationException.class, () -> target.getErrors().put("key", "value"));
    }

    @Test
    void constructor_withNullMap_shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () ->  {
            throw new ErrorsMapException(null);
        });
    }

    @Test
    void constructor_withNullKeyOrValue_shouldThrowNullPointerException() {
        String key = "key";
        String value = "value";

        assertThrows(NullPointerException.class, () -> {
            throw new ErrorsMapException(key, null);
        });

        assertThrows(NullPointerException.class, () -> {
            throw new ErrorsMapException(null, value);
        });

        assertThrows(NullPointerException.class, () -> {
            throw new ErrorsMapException(null, null);
        });
    }
}