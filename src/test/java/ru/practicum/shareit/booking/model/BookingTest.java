package ru.practicum.shareit.booking.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingTest {
    final Long id = 1L;
    final LocalDateTime start = LocalDateTime.now().plusDays(1);
    final LocalDateTime end = LocalDateTime.now().plusDays(2);
    final BookingStatus status = BookingStatus.WAITING;
    final Item item = createItem(2L, "itemName", "itemDescription", true,
            createUser(3L, "ownerName", "owner@email.com"), null);
    final User booker = createUser(4L, "authorName", "author@email.com");

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjectWithNullIdAndOtherFields_shouldReturnTrue() {
        Booking booking = createBooking(null, null, null, null, null, null);

        assertThat(booking.equals(booking), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNullAndNullIdAndOtherFields_shouldReturnFalse() {
        Booking booking = createBooking(null, null, null, null, null, null);

        assertThat(booking.equals(null), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withObjectOfOtherClassWithNotNullIdAndNullOtherFields_shouldReturnFalse() {
        Booking booking = createBooking(id, null, null, null, null, null);
        User user = createUser(id, null, null);

        assertThat(booking.equals(user), is(false));
    }

    @Test
    void equals_withNullIdsAndNotNullEqualOtherFields_shouldReturnFalse() {
        Booking booking1 = createBooking(null, start, end, status, item, booker);
        Booking booking2 = createBooking(null, start, end, status, item, booker);

        assertThat(booking1.equals(booking2), is(false));
    }

    @Test
    void equals_withNotNullEqualIdsAndNotEqualOtherFields_shouldReturnTrue() {
        Booking booking1 = createBooking(id, null, null, null, null, null);
        Booking booking2 = createBooking(id, start, end, status, item, booker);

        assertThat(booking1.equals(booking2), is(true));
    }

    @ParameterizedTest(name = "ids={0}")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoBookingsWithEqualIdsAndNotEqualOtherFields_shouldBeEqual(Long id) {
        Booking booking1 = createBooking(id, start, end, status, item, booker);
        Booking booking2 = createBooking(id, null, null, null, null, null);

        assertThat(booking1.hashCode(), equalTo(booking2.hashCode()));
    }

    @Test
    void hashCode_ofTwoBookingsWithNullAndZeroIdsAndNotEqualOtherFields_shouldBeEqual() {
        Booking booking1 = createBooking(null, start, end, status, item, booker);
        Booking booking2 = createBooking(0L, null, null, null, null, null);

        assertThat(booking1.hashCode(), equalTo(booking2.hashCode()));
    }

    @ParameterizedTest(name = "id1={0}, id2=2")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoBookingsWithNotEqualIdsAndEqualOtherFields_shouldNotBeEqual(Long id) {
        Booking booking1 = createBooking(id, start, end, status, item, booker);
        Booking booking2 = createBooking(2L, start, end, status, item, booker);

        assertThat(booking1.hashCode(), not(equalTo(booking2.hashCode())));
    }
}