package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bookings")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    DefaultLocaleMessageSource messageSource;

    BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDtoToClient> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @Valid @RequestBody BookingDtoFromClient bookingDtoFromClient) {
        BookingDtoToClient bookingDtoToClient = bookingService.create(userId, bookingDtoFromClient);
        log.info("{}: {}", messageSource.get("booking.BookingController.create"), bookingDtoToClient);
        return ResponseEntity.ok(bookingDtoToClient);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingDtoToClient> approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable Long id,
                                                      @RequestParam(value = "approved") boolean approved) {
        BookingDtoToClient bookingDtoToClient = bookingService.approve(id, userId, approved);
        log.info("{}: {}", messageSource.get("booking.BookingController.approve"), bookingDtoToClient);
        return ResponseEntity.ok(bookingDtoToClient);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDtoToClient> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long id) {
        BookingDtoToClient bookingDtoToClient = bookingService.readById(id, userId);
        log.info("{}: {}", messageSource.get("booking.BookingController.readById"), bookingDtoToClient);
        return ResponseEntity.ok(bookingDtoToClient);
    }

    @GetMapping
    public ResponseEntity<Set<BookingDtoToClient>> readByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestParam(value = "state", required = false)
                                                        String state) {
        Set<BookingDtoToClient> bookings = bookingService.readByBooker(userId, state);
        log.info("{} ({}, {}): {}", messageSource.get("booking.BookingController.readByBooker"), userId, state,
                bookings.stream().map(BookingDtoToClient::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<Set<BookingDtoToClient>> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(value = "state", required = false)
                                                        String state) {
        Set<BookingDtoToClient> bookings = bookingService.readByOwner(userId, state);
        log.info("{} ({}, {}): {}", messageSource.get("booking.BookingController.readByOwner"), userId, state,
                bookings.stream().map(BookingDtoToClient::getId).collect(Collectors.toSet()));
        return ResponseEntity.ok(bookings);
    }
}
