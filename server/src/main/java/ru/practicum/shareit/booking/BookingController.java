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
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    DefaultLocaleMessageSource messageSource;

    BookingService service;

    @PostMapping
    public ResponseEntity<BookingDtoToClient> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestBody BookingDtoFromClient inDto) {
        BookingDtoToClient outDto = service.create(userId, inDto);
        log.info("{}: {}", messageSource.get("booking.BookingController.create"), outDto);
        return ResponseEntity.ok(outDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDtoToClient>> readByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                 @RequestParam(value = "from", required = false,
                                                                         defaultValue = "0")
                                                                 Integer from,
                                                                 @RequestParam(value = "size", required = false)
                                                                 Integer size,
                                                                 @RequestParam(value = "state") BookingState state) {
        List<BookingDtoToClient> dtoList = service.readByBooker(userId, state, from, size);
        log.info("{} ({}, {}): {}", messageSource.get("booking.BookingController.readByBooker"), userId, state,
                dtoList.stream().map(BookingDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDtoToClient>> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                @RequestParam(value = "from", required = false,
                                                                        defaultValue = "0")
                                                                Integer from,
                                                                @RequestParam(value = "size", required = false)
                                                                Integer size,
                                                                @RequestParam(value = "state") BookingState state) {
        List<BookingDtoToClient> dtoList = service.readByOwner(userId, state, from, size);
        log.info("{} ({}, {}): {}", messageSource.get("booking.BookingController.readByOwner"), userId, state,
                dtoList.stream().map(BookingDtoToClient::getId).collect(Collectors.toList()));
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDtoToClient> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @PathVariable Long id) {
        BookingDtoToClient dto = service.readById(id, userId);
        log.info("{}: {}", messageSource.get("booking.BookingController.readById"), dto);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookingDtoToClient> approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable Long id,
                                                      @RequestParam(value = "approved") boolean approved) {
        BookingDtoToClient dto = service.approve(id, userId, approved);
        log.info("{}: {}", messageSource.get("booking.BookingController.approve"), dto);
        return ResponseEntity.ok(dto);
    }
}