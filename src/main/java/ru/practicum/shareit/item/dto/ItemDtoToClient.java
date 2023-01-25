package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoToClient {
    Long id;

    String name;

    String description;

    Boolean available;

    BookingDto lastBooking;

    BookingDto nextBooking;

    Set<CommentDtoToClient> comments;
}
