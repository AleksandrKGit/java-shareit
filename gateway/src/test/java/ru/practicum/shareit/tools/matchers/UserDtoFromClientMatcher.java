package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.user.UserDtoFromClient;
import java.util.Objects;

public class UserDtoFromClientMatcher implements ArgumentMatcher<UserDtoFromClient> {
    private final UserDtoFromClient dto;

    private UserDtoFromClientMatcher(UserDtoFromClient dto) {
        this.dto = dto;
    }

    public static UserDtoFromClientMatcher equalToDto(UserDtoFromClient dto) {
        return new UserDtoFromClientMatcher(dto);
    }

    @Override
    public boolean matches(UserDtoFromClient dto) {
        return dto != null && this.dto != null
                && Objects.equals(this.dto.getName(), dto.getName())
                && Objects.equals(this.dto.getEmail(), dto.getEmail());
    }
}