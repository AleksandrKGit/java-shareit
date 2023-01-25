package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class CommentMapper {
    public static CommentDtoToClient toCommentDto(Comment comment) {
        return comment == null ? null : new CommentDtoToClient(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentDtoFromClient commentDtoFromClient) {
        return commentDtoFromClient == null ? null : new Comment(
                commentDtoFromClient.getId(),
                commentDtoFromClient.getText(),
                Item.builder().id(commentDtoFromClient.getItemId()).build(),
                User.builder().id(commentDtoFromClient.getAuthorId()).build(),
                null
        );
    }
}
