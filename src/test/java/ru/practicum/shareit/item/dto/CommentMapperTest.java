package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Comment;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@SpringBootTest(classes = {CommentMapperImpl.class})
class CommentMapperTest {
    @Autowired
    private CommentMapper commentMapper;

    @Test
    void toDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        Comment source = createComment(10L, "commentText", LocalDateTime.now(),
                createUser(20L, "authorName", null), null);

        CommentDtoToClient target = commentMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("text", equalTo(source.getText())),
                hasProperty("created", equalTo(source.getCreated())),
                hasProperty("authorName", equalTo(source.getAuthor().getName()))
        ));
    }

    @Test
    void toDto_withNull_shouldReturnNull() {
        assertThat(commentMapper.toDto(null), is(nullValue()));
    }

    @Test
    void toDto_withNullFields_shouldReturnDtoWithNullFields() {
        Comment source = createComment(null, null, null, null, null);

        CommentDtoToClient target = commentMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("text", is(nullValue())),
                hasProperty("created", is(nullValue())),
                hasProperty("authorName", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFields() {
        CommentDtoFromClient source = createCommentDtoFromClient("commentText");

        Comment target = commentMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("text", equalTo(source.getText())),
                hasProperty("item", is(nullValue())),
                hasProperty("author", is(nullValue())),
                hasProperty("created", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(commentMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        CommentDtoFromClient source = createCommentDtoFromClient(null);

        Comment target = commentMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("text", equalTo(source.getText())),
                hasProperty("item", is(nullValue())),
                hasProperty("author", is(nullValue())),
                hasProperty("created", is(nullValue()))
        ));
    }
}