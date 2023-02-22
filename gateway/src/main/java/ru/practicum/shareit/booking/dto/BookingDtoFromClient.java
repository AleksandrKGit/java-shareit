package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDtoFromClient {
	@NotNull(message = "{booking.BookingDto.notNullStart}")
	@FutureOrPresent(message = "{booking.BookingDto.notInPastStart}")
	LocalDateTime start;

	@NotNull(message = "{booking.BookingDto.notNullEnd}")
	@FutureOrPresent(message = "{booking.BookingDto.notInPastEnd}")
	LocalDateTime end;

	@NotNull(message = "{booking.BookingDto.notNullItemId}")
	Long itemId;
}