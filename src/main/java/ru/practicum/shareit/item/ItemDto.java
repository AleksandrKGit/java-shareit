package ru.practicum.shareit.item;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class ItemDto {
    Integer id;
    @NullOrNotBlank(message = "{item.ItemDto.notBlankName}")
    String name;
    String description;
    Boolean available;
    Integer owner;
    Integer request;
}
