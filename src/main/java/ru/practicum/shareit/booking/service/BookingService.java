package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import java.util.Set;

@Service
public interface BookingService {
    BookingDtoToClient create(BookingDtoFromClient bookingDtoFromClient);

    BookingDtoToClient approve(Long id, Long bookerId, boolean approved);

    BookingDtoToClient readById(Long id, Long userId);

    Set<BookingDtoToClient> readByBooker(Long bookerId, BookingState state);

    Set<BookingDtoToClient> readByOwner(Long ownerId, BookingState state);
}