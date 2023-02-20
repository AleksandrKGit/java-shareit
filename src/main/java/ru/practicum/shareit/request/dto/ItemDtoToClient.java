package ru.practicum.shareit.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoToClient {
    Long id;

    String name;

    String description;

    Boolean available;

    Long requestId;
}