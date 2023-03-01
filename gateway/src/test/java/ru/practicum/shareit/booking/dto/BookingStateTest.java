package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class BookingStateTest {
    private static Stream<String> bookingState() {
        return Arrays.stream(BookingState.values()).map(Enum::toString);
    }

    @ParameterizedTest
    @MethodSource("bookingState")
    void from_withValidState_shouldReturnOptionalPresentedByBookingState(String stateString) {
        Optional<BookingState> target = BookingState.from(stateString);

        assertThat(target.isPresent(), is(true));
        assertThat(target.get().toString(), equalTo(stateString));
    }

    @Test
    void from_withInvalidState_shouldReturnEmptyOptional() {
        Optional<BookingState> target = BookingState.from("invalidState");

        assertThat(target.isEmpty(), is(true));
    }
}