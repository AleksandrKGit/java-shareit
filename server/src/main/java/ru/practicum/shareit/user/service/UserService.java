package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;
import java.util.List;

public interface UserService {
    UserDtoToClient create(UserDtoFromClient dto);

    List<UserDtoToClient> readAll();

    UserDtoToClient readById(Long id);

    UserDtoToClient update(Long id, UserDtoFromClient dto);

    void delete(Long id);
}
