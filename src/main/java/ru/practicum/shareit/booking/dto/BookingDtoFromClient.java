package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
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