package ru.practicum.shareit.request.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ItemRequestDtoFromClient {
    static final int MAX_DESCRIPTION_SIZE = 255;

    @Size(max = MAX_DESCRIPTION_SIZE, message = "{request.ItemRequestDto.descriptionSize}: " + MAX_DESCRIPTION_SIZE)
    @NotBlank(message = "{request.ItemRequestDto.notBlankDescription}")
    private String description;
}
