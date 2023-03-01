package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import java.util.Objects;

public class CommentDtoFromClientMatcher implements ArgumentMatcher<CommentDtoFromClient> {
    private final CommentDtoFromClient dto;

    private CommentDtoFromClientMatcher(CommentDtoFromClient dto) {
        this.dto = dto;
    }

    public static CommentDtoFromClientMatcher equalToDto(CommentDtoFromClient dto) {
        return new CommentDtoFromClientMatcher(dto);
    }

    @Override
    public boolean matches(CommentDtoFromClient dto) {
        return dto != null && this.dto != null
                && Objects.equals(this.dto.getText(), dto.getText());
    }
}