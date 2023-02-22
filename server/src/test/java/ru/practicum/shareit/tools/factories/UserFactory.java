package ru.practicum.shareit.tools.factories;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;

public class UserFactory {
    public static User copyOf(User user) {
        if (user == null) {
            return null;
        }

        User copy = new User();
        copy.setId(user.getId());
        copy.setName(user.getName());
        copy.setEmail(user.getEmail());
        return copy;
    }

    public static User createUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    public static UserDtoFromClient createUserDtoFromClient(String name, String email) {
        UserDtoFromClient dto = new UserDtoFromClient();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    public static UserDtoToClient createUserDtoToClient(Long id, String name, String email) {
        UserDtoToClient dto = new UserDtoToClient();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
