package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDtoFromClient {
    static final int MAX_NAME_SIZE = 255;

    static final int MAX_EMAIL_SIZE = 255;

    @Size(groups = {OnCreate.class, OnUpdate.class}, max = MAX_NAME_SIZE,
            message = "{user.UserDto.nameSize}: " + MAX_NAME_SIZE)
    @NotBlank(groups = OnCreate.class, message = "{user.UserDto.notBlankName}")
    @NullOrNotBlank(groups = OnUpdate.class, message = "{user.UserDto.notBlankName}")
    String name;

    @NotNull(groups = OnCreate.class, message = "{user.UserDto.notNullEmail}")
    @Size(groups = {OnCreate.class, OnUpdate.class}, max = MAX_EMAIL_SIZE,
            message = "{user.UserDto.emailSize}: " + MAX_EMAIL_SIZE)
    @Email(groups = {OnCreate.class, OnUpdate.class}, message = "{user.UserDto.incorrectEmail}",
            regexp = "^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$")
    String email;
}