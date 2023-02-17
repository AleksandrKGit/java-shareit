package ru.practicum.shareit.tools.factories;

import ru.practicum.shareit.booking.dto.BookerDtoToClient;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.dto.ItemDtoToClient;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;

public class BookingFactory {
    public static BookingDtoToClient createBookingDtoToClient(Long id, LocalDateTime start, LocalDateTime end,
                                                                BookingStatus status, ItemDtoToClient item,
                                                                BookerDtoToClient booker) {
        BookingDtoToClient bookingDto = new BookingDtoToClient();
        bookingDto.setId(id);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setStatus(status);
        bookingDto.setItem(item);
        bookingDto.setBooker(booker);
        return bookingDto;
    }

    public static BookingDtoFromClient createBookingDtoFromClient(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDtoFromClient bookingDto = new BookingDtoFromClient();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    public static ItemDtoToClient createItemDtoToClient(Long id, String name) {
        ItemDtoToClient itemDto = new ItemDtoToClient();
        itemDto.setId(id);
        itemDto.setName(name);
        return  itemDto;
    }

    public static BookerDtoToClient createBookerDtoToClient(Long id) {
        BookerDtoToClient bookerDto = new BookerDtoToClient();
        bookerDto.setId(id);
        return bookerDto;
    }

    public static Booking copyOf(Booking booking) {
        Booking copy = new Booking();
        copy.setId(booking.getId());
        copy.setItem(ItemFactory.copyOf(booking.getItem()));
        copy.setStatus(booking.getStatus());
        copy.setStart(booking.getStart());
        copy.setEnd(booking.getEnd());
        copy.setBooker(UserFactory.copyOf(booking.getBooker()));
        return copy;
    }

    public static Booking createBooking(Long id, LocalDateTime start, LocalDateTime end, BookingStatus status,
                                        Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setStatus(status);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBooker(booker);
        return booking;
    }
}
