package ru.practicum.shareit.support;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.util.stream.Stream;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class ConstraintCheckerTest {
    private static Stream<Arguments> checkNotContains() {
        return Stream.of(
                Arguments.of("null exception",
                        null),

                Arguments.of("null message and null cause",
                        new Exception()),

                Arguments.of("null cause and not in message",
                        new Exception("exception")),

                Arguments.of("cause with null message and not in exception's messages",
                        new Exception("exception", new Exception())),

                Arguments.of("null subCause and not in messages",
                        new Exception("exception", new Exception("cause"))),

                Arguments.of("subCause with null message and not in other messages",
                        new Exception("exception", new Exception("cause", new Exception()))),

                Arguments.of("not in exception, cause and subCause messages",
                        new Exception("exception", new Exception("cause", new Exception("subCause"))))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("checkNotContains")
    void check_withNullExceptionOrExceptionAndNestedExceptionsMessagesNotContainingConstrainName_shouldReturnFalse(
            String testName, Exception exception) {
        assertThat(ConstraintChecker.check(exception, "Email"), is(false));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void check_withNullOrEmptyConstrainName_shouldReturnFalse(String constraintName) {
        assertThat(ConstraintChecker.check(new Exception("Incorrect email"), constraintName), is(false));
    }

    private static Stream<Arguments> checkContains() {
        return Stream.of(
                Arguments.of("in exception's message",
                        new Exception(" _eMAIL_ constraint")),
                Arguments.of("in cause's message",
                        new Exception("exception", new Exception("coNemAILstRaInt"))),
                Arguments.of("in subCause's message",
                        new Exception("exception", new Exception("cause", new Exception("some email"))))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("checkContains")
    void check_withExceptionOrNestedExceptionsContainingNotNullAndNotEmptyConstrainNameInMessage_shouldReturnTrue(
            String testName, Exception exception) {
        assertThat(ConstraintChecker.check(exception, "Email"), is(true));
    }
}