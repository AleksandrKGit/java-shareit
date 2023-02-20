package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.user.User;
import java.util.Objects;

public class UserMatcher implements ArgumentMatcher<User> {
    private final User entity;

    private UserMatcher(User entity) {
        this.entity = entity;
    }

    public static UserMatcher equalToUser(User entity) {
        return new UserMatcher(entity);
    }

    @Override
    public boolean matches(User entity) {
        return entity != null && this.entity != null
                && Objects.equals(this.entity.getId(), entity.getId())
                && Objects.equals(this.entity.getEmail(), entity.getEmail())
                && Objects.equals(this.entity.getName(), entity.getName());
    }
}