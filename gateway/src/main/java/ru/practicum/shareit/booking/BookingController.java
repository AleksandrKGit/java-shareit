package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import javax.validation.Valid;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
	BookingClient bookingClient;

	DefaultLocaleMessageSource messageSource;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
										 @Valid @RequestBody BookingDtoFromClient inDto) {
		if (!inDto.getStart().isBefore(inDto.getEnd())) {
			throw new ValidationException("start", messageSource.get("booking.BookingController.startBeforeEnd") + ": "
					+ inDto.getStart() + " " + inDto.getEnd());
		}

		log.info("{}: {}", messageSource.get("booking.BookingController.create"), inDto);
		return bookingClient.create(userId, inDto);
	}

	@GetMapping
	public ResponseEntity<Object> readByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
											   @RequestParam(value = "from", required = false, defaultValue = "0")
											   @Min(value = 0, message = "{controller.minFrom}") Integer from,
											   @RequestParam(value = "size", required = false)
											   @Min(value = 1, message = "{controller.minSize}") Integer size,
											   @RequestParam(value = "state", required = false, defaultValue = "all")
											   String stateParam) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ValidationException("error", "Unknown state: " + stateParam));

		log.info("{}: {}, {}, {}, {}", messageSource.get("booking.BookingController.readByBooker"), userId, state, from,
				size);
		return bookingClient.readByBooker(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> readByOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
											  @RequestParam(value = "from", required = false, defaultValue = "0")
											  @Min(value = 0, message = "{controller.minFrom}") Integer from,
											  @RequestParam(value = "size", required = false)
											  @Min(value = 1, message = "{controller.minSize}") Integer size,
											  @RequestParam(value = "state", defaultValue = "all", required = false)
											  String stateParam) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new ValidationException("error", "Unknown state: " + stateParam));

		log.info("{}: {}, {}, {}, {}", messageSource.get("booking.BookingController.readByOwner"), userId, state, from,
				size);
		return bookingClient.readByOwner(userId, state, from, size);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> readById(@RequestHeader("X-Sharer-User-Id") Long userId,
										   @PathVariable Long id) {
		log.info("{}: {}, {}", messageSource.get("booking.BookingController.readById"), userId, id);
		return bookingClient.readById(userId, id);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<Object> approve(@RequestHeader("X-Sharer-User-Id") Long userId,
										  @PathVariable Long id,
										  @RequestParam(value = "approved") boolean approved) {
		log.info("{}: {}, {}, {}", messageSource.get("booking.BookingController.approve"), id, userId, approved);
		return bookingClient.approve(userId, id, approved);
	}
}