package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import java.util.Set;

public interface BookingService {
    BookingDtoToClient create(Long bookerId, BookingDtoFromClient bookingDtoFromClient);

    BookingDtoToClient approve(Long id, Long bookerId, boolean approved);

    BookingDtoToClient readById(Long id, Long userId);

    Set<BookingDtoToClient> readByBooker(Long bookerId, String state);

    Set<BookingDtoToClient> readByOwner(Long ownerId, String state);
}