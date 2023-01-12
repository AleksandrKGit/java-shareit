package ru.practicum.shareit.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class ItemRequestDto {
    Integer id;
    @NullOrNotBlank(message = "{request.ItemRequestDto.notBlankDescription}")
    String description;
    Integer requestor;
    LocalDateTime created;
}
