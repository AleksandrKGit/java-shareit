package ru.practicum.shareit.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.BookingStatus;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDtoToClient {
    Long id;

    @FutureOrPresent(message = "{booking.BookingDto.notInPastStart}")
    LocalDateTime start;

    @FutureOrPresent(message = "{booking.BookingDto.notInPastEnd}")
    LocalDateTime end;

    ItemDtoToClient item;

    BookerDtoToClient booker;

    BookingStatus status;
}