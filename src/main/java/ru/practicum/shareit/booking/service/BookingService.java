package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import java.util.List;

public interface BookingService {
    BookingDtoToClient create(Long bookerId, BookingDtoFromClient bookingDtoFromClient);

    List<BookingDtoToClient> readByBooker(Long bookerId, String state, Integer from, Integer size);

    List<BookingDtoToClient> readByOwner(Long ownerId, String state, Integer from, Integer size);

    BookingDtoToClient readById(Long id, Long userId);

    BookingDtoToClient approve(Long id, Long bookerId, boolean approved);
}