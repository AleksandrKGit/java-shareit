package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.User;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.practicum.shareit.tools.factories.UserFactory.*;

@SpringBootTest(classes = {UserMapperImpl.class})
class UserMapperTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    void toDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        User source = createUser(10L, "userName", "user@email.com");

        UserDtoToClient target = userMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("email", equalTo(source.getEmail()))
        ));
    }

    @Test
    void toDto_withNull_shouldReturnNull() {
        assertThat(userMapper.toDto(null), is(nullValue()));
    }

    @Test
    void toDto_withNullFields_shouldReturnDtoWithNullFields() {
        User source = createUser(null, null, null);

        UserDtoToClient target = userMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("email", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFields() {
        UserDtoFromClient source = createUserDtoFromClient("userName", "user@email.com");

        User target = userMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("email", equalTo(source.getEmail()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(userMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        UserDtoFromClient source = createUserDtoFromClient(null, null);

        User target = userMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("email", is(nullValue()))
        ));
    }

    @Test
    void updateEntityFromDto_withNullFields_shouldNotUpdateEntity() {
        UserDtoFromClient source = createUserDtoFromClient(null, null);
        User target = createUser(10L, "userName", "user@email.com");

        userMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", is(not(nullValue()))),
                hasProperty("email", is(not(nullValue())))
        ));
    }

    @Test
    void updateEntityFromDto_withNotNullFields_shouldUpdateEntity() {
        UserDtoFromClient source = createUserDtoFromClient("newName", "new@email.com");
        User target = createUser(10L, "userName", "user@email.com");

        userMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("email", equalTo(source.getEmail()))
        ));
    }
}