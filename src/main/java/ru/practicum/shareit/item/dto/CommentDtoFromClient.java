package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDtoFromClient {
    static final int MAX_TEXT_SIZE = 2000;

    Long id;

    @Size(max = MAX_TEXT_SIZE, message = "{comment.CommentDto.textSize}: " + MAX_TEXT_SIZE)
    @NotBlank(message = "{comment.CommentDto.notBlankText}")
    String text;

    Long itemId;

    Long authorId;
}
