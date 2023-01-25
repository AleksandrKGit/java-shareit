package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoFromClient {
    static final int MAX_NAME_SIZE = 255;
    static final int MAX_DESCRIPTION_SIZE = 2000;

    Long id;

    @Size(max = MAX_NAME_SIZE, message = "{item.ItemDto.nameSize}: " + MAX_NAME_SIZE)
    @NullOrNotBlank(message = "{item.ItemDto.notBlankName}")
    String name;

    @Size(max = MAX_DESCRIPTION_SIZE, message = "{item.ItemDto.descriptionSize}: " + MAX_DESCRIPTION_SIZE)
    String description;

    Boolean available;

    Long ownerId;

    Long requestId;
}
