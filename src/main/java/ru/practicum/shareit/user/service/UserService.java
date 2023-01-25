package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserDto;
import java.util.Set;

@Service
public interface UserService {
    UserDto create(UserDto userDto);

    Set<UserDto> readAll();

    UserDto readById(Long id);

    UserDto update(UserDto userDto);

    void delete(Long id);
}
