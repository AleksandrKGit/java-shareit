package ru.practicum.shareit.user.dto;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDtoToClient toDto(User entity);

    User toEntity(UserDtoFromClient dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDtoFromClient dto, @MappingTarget User entity);
}
