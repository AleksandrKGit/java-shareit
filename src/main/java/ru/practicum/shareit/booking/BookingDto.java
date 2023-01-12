package ru.practicum.shareit.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class BookingDto {
    Integer id;
    LocalDateTime start;
    LocalDateTime end;
    Integer item;
    Integer booker;
    BookingStatus status;
}