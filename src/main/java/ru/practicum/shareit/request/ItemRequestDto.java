package ru.practicum.shareit.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class ItemRequestDto {
    private final static int MAX_DESCRIPTION_SIZE = 255;

    Long id;

    @Size(max = MAX_DESCRIPTION_SIZE, message = "{request.ItemRequestDto.descriptionSize}: " + MAX_DESCRIPTION_SIZE)
    @NullOrNotBlank(message = "{request.ItemRequestDto.notBlankDescription}")
    String description;

    Long requestorId;

    LocalDateTime created;
}
