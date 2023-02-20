package ru.practicum.shareit.user.dto;

import org.mapstruct.*;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    List<UserDtoToClient> toDtoList(List<User> entities);

    UserDtoToClient toDto(User entity);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserDtoFromClient dto);

    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDtoFromClient dto, @MappingTarget User entity);
}