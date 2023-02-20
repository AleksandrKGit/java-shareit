package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBookingDtoFromClient;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@SpringBootTest(classes = {BookingMapperImpl.class})
class BookingMapperTest {
    @Autowired
    private BookingMapper bookingMapper;

    @Test
    void toDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        Booking source = createBooking(10L, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                BookingStatus.WAITING, createItem(20L, "itemName", null, true,
                        null, null), createUser(30L, null, null));

        BookingDtoToClient target = bookingMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("start", equalTo(source.getStart())),
                hasProperty("end", equalTo(source.getEnd())),
                hasProperty("status", equalTo(source.getStatus())),
                hasProperty("booker", hasProperty("id", equalTo(source.getBooker().getId()))),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(source.getItem().getId())),
                        hasProperty("name", equalTo(source.getItem().getName()))
                ))
        ));
    }

    @Test
    void toDto_withNull_shouldReturnNull() {
        assertThat(bookingMapper.toDto(null), is(nullValue()));
    }

    @Test
    void toDto_withNullFields_shouldReturnDtoWithNullFields() {
        Booking source = createBooking(null, null, null, null, null, null);

        BookingDtoToClient target = bookingMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("start", is(nullValue())),
                hasProperty("end", is(nullValue())),
                hasProperty("status", is(nullValue())),
                hasProperty("booker", is(nullValue())),
                hasProperty("item", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFieldsAndNullItem() {
        BookingDtoFromClient source = createBookingDtoFromClient(10L, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1));

        Booking target = bookingMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("start", equalTo(source.getStart())),
                hasProperty("end", equalTo(source.getEnd())),
                hasProperty("status", is(nullValue())),
                hasProperty("booker", is(nullValue())),
                hasProperty("item", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(bookingMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        BookingDtoFromClient source = createBookingDtoFromClient(null, null, null);

        Booking target = bookingMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("start", is(nullValue())),
                hasProperty("end", is(nullValue())),
                hasProperty("status", is(nullValue())),
                hasProperty("booker", is(nullValue())),
                hasProperty("item", is(nullValue()))
        ));
    }
}