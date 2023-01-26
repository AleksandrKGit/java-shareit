package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.model.Comment;

@Mapper
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(target = "authorName", source = "author.name")
    CommentDtoToClient toDto(Comment comment);

    @Mapping(target = "author.id", source = "authorId")
    @Mapping(target = "item.id", source = "itemId")
    Comment toModel(CommentDtoFromClient commentDto);
}
