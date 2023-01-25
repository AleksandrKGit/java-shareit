package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {
    public static BookingDtoToClient toBookingDto(Booking booking) {
        return booking == null ? null : new BookingDtoToClient(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                new ItemDtoToClient(booking.getItem().getId(), booking.getItem().getName()),
                new BookerDtoToClient(booking.getBooker().getId()),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDtoFromClient bookingDtoFromClient) {
        return bookingDtoFromClient == null ? null : new Booking(
                bookingDtoFromClient.getId(),
                bookingDtoFromClient.getStart(),
                bookingDtoFromClient.getEnd(),
                Item.builder().id(bookingDtoFromClient.getItemId()).build(),
                User.builder().id(bookingDtoFromClient.getBookerId()).build(),
                bookingDtoFromClient.getStatus()
        );
    }
}