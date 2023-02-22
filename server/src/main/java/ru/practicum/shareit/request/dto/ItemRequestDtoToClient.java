package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestDtoToClient {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    Long id;

    String description;

    @JsonFormat(pattern = DATE_PATTERN)
    LocalDateTime created;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<ItemDtoToClient> items;
}
