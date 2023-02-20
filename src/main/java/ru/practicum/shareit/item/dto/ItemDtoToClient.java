package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoToClient {
    Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long requestId;

    String name;

    String description;

    Boolean available;

    BookingDtoToClient lastBooking;

    BookingDtoToClient nextBooking;

    List<CommentDtoToClient> comments;
}
