package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDtoToClient {
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    Long id;

    @JsonFormat(pattern = DATE_PATTERN)
    LocalDateTime start;

    @JsonFormat(pattern = DATE_PATTERN)
    LocalDateTime end;

    ItemDtoToClient item;

    BookerDtoToClient booker;

    BookingStatus status;
}