package ru.practicum.shareit.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.validation.constraints.NullOrNotBlank;
import javax.validation.constraints.Email;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class UserDto {
    Integer id;
    @NullOrNotBlank(message = "{user.UserDto.notBlankName}")
    String name;
    @Email(message = "{user.UserDto.incorrectEmail}")
    String email;
}