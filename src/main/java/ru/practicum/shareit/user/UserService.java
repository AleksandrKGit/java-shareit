package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public interface UserService {
    UserDto create(UserDto userDto);

    Set<UserDto> readAll();

    UserDto readById(Integer id);

    UserDto update(UserDto userDto);

    void delete(Integer id);
}
