package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.booking.model.Booking;
import java.util.Objects;

public class BookingMatcher implements ArgumentMatcher<Booking> {
    private final Booking entity;

    private BookingMatcher(Booking entity) {
        this.entity = entity;
    }

    public static BookingMatcher equalToBooking(Booking entity) {
        return new BookingMatcher(entity);
    }

    @Override
    public boolean matches(Booking entity) {
        return entity != null && this.entity != null
                && Objects.equals(this.entity.getId(), entity.getId())
                && Objects.equals(this.entity.getStart(), entity.getStart())
                && Objects.equals(this.entity.getEnd(), entity.getEnd())
                && Objects.equals(this.entity.getStatus(), entity.getStatus())
                && Objects.equals(this.entity.getItem(), entity.getItem())
                && Objects.equals(this.entity.getBooker(), entity.getBooker());
    }
}
