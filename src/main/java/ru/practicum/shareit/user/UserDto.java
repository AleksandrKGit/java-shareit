package ru.practicum.shareit.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    private final static int MAX_NAME_SIZE = 255;

    private final static int MAX_EMAIL_SIZE = 255;

    Long id;

    @Size(max = MAX_NAME_SIZE, message = "{user.UserDto.nameSize}: " + MAX_NAME_SIZE)
    @NullOrNotBlank(message = "{user.UserDto.notBlankName}")
    String name;

    @Size(max = MAX_EMAIL_SIZE, message = "{user.UserDto.emailSize}: " + MAX_EMAIL_SIZE)
    @Email(message = "{user.UserDto.incorrectEmail}")
    String email;
}