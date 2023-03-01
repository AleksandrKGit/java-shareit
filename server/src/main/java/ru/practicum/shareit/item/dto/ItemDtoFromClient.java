package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoFromClient {
    String name;

    String description;

    Boolean available;

    Long requestId;
}
