package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.UserDto;
import java.util.Set;

public interface UserService {
    UserDto create(UserDto userDto);

    Set<UserDto> readAll();

    UserDto readById(Long id);

    UserDto update(Long id, UserDto userDto);

    void delete(Long id);
}
