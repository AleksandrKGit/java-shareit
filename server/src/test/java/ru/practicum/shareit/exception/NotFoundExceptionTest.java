package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotFoundExceptionTest {
    @Test
    void constructor_withNullArgument_shouldThrowNullPointerException() {
        String key = "key";
        String value = "value";

        assertThrows(NullPointerException.class, () -> {
            throw new NotFoundException(key, null);
        });

        assertThrows(NullPointerException.class, () -> {
            throw new NotFoundException(null, value);
        });

        assertThrows(NullPointerException.class, () -> {
            throw new NotFoundException(null, null);
        });
    }

    @Test
    void constructor_withNotNullArguments_shouldSaveArgumentsToSortedUnmodifiableMapOfErrors() {
        String key = "key";
        String value = "value";
        Map<String, String> expectingErrors = Map.of(key, value);

        NotFoundException target = new NotFoundException(key, value);

        assertThat(target, allOf(
                hasProperty("errors", equalTo(expectingErrors)),
                hasProperty("message", equalTo(expectingErrors.toString()))
        ));

        assertThrows(UnsupportedOperationException.class, () -> target.getErrors().put("newKey", "newValue"));
    }
}