package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDtoToClient {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    Long id;

    String text;

    String authorName;

    @JsonFormat(pattern = DATE_PATTERN)
    LocalDateTime created;
}
