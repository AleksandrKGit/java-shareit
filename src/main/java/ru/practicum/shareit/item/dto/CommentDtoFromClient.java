package ru.practicum.shareit.item.dto;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CommentDtoFromClient {
    static final int MAX_TEXT_SIZE = 2000;

    @Size(max = MAX_TEXT_SIZE, message = "{comment.CommentDto.textSize}: " + MAX_TEXT_SIZE)
    @NotBlank(message = "{comment.CommentDto.notBlankText}")
    private String text;
}
