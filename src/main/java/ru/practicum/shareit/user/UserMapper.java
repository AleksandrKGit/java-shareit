package ru.practicum.shareit.user;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return user == null ? null : new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDto userDto) {
        return userDto == null ? null : new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }
}
