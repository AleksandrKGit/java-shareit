package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoFromClient {
    static final int MAX_NAME_SIZE = 255;
    static final int MAX_DESCRIPTION_SIZE = 2000;

    @Size(groups = {OnCreate.class, OnUpdate.class}, max = MAX_NAME_SIZE,
            message = "{item.ItemDto.nameSize}: " + MAX_NAME_SIZE)
    @NotBlank(groups = OnCreate.class, message = "{item.ItemDto.notBlankName}")
    @NullOrNotBlank(groups = OnUpdate.class, message = "{item.ItemDto.notBlankName}")
    String name;

    @NotNull(groups = OnCreate.class, message = "{item.ItemDto.notNullDescription}")
    @Size(groups = {OnCreate.class, OnUpdate.class}, max = MAX_DESCRIPTION_SIZE,
            message = "{item.ItemDto.descriptionSize}: " + MAX_DESCRIPTION_SIZE)
    String description;

    @NotNull(groups = OnCreate.class, message = "{item.ItemDto.notNullAvailable}")
    Boolean available;

    Long requestId;
}
