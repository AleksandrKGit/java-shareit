package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentDtoToClient toDto(Comment entity);

    Comment toEntity(CommentDtoFromClient dto);
}
