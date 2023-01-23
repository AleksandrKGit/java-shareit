package ru.practicum.shareit.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class BookingDto {
    Integer id;
    @FutureOrPresent(message = "{booking.BookingDto.notInPastStart}")
    LocalDateTime start;
    @FutureOrPresent(message = "{booking.BookingDto.notInPastEnd}")
    LocalDateTime end;
    Integer item;
    Integer booker;
    BookingStatus status;
}