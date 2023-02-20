package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.request.ItemRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
class UserTest {
    final Long id = 1L;
    final String email = "user@email.com";
    final String name = "UserName";

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjectWithNullIdAndOtherFields_shouldReturnTrue() {
        User user = createUser(null, null, null);

        assertThat(user.equals(user), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNullAndNullIdAndOtherFields_shouldReturnFalse() {
        User user = createUser(null, null, null);

        assertThat(user.equals(null), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withObjectOfOtherClassWithNotNullIdsAndNullOtherFields_shouldReturnFalse() {
        User user = createUser(id, null, null);
        ItemRequest itemRequest = createItemRequest(id, null, null, null);

        assertThat(user.equals(itemRequest), is(false));
    }

    @Test
    void equals_withNullIdsAndNotNullEqualOtherFields_shouldReturnFalse() {
        User user1 = createUser(null, name, email);
        User user2 = createUser(null, name, email);

        assertThat(user1.equals(user2), is(false));
    }

    @Test
    void equals_withNotNullEqualIdsAndNotEqualOtherFields_shouldReturnTrue() {
        User user1 = createUser(id, null, null);
        User user2 = createUser(id, name, email);

        assertThat(user1.equals(user2), is(true));
    }

    @ParameterizedTest(name = "Entities with id={0} and id={0}")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_withEqualIdsAndNotEqualOtherFields_shouldBeEqual(Long id) {
        User user1 = createUser(id, name, email);
        User user2 = createUser(id, null, null);

        assertThat(user1.hashCode(), equalTo(user2.hashCode()));
    }

    @Test
    void hashCode_ofTwoUsersWithNullAndZeroIdsAndNotEqualOtherFields_shouldBeEqual() {
        User user1 = createUser(null, name, email);
        User user2 = createUser(0L, null, null);

        assertThat(user1.hashCode(), equalTo(user2.hashCode()));
    }

    @ParameterizedTest(name = "Entities with id={0}, id2=2")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoUsersWithNotEqualIdsAndEqualOtherFields_shouldNotBeEqual(Long id) {
        User user1 = createUser(id, name, email);
        User user2 = createUser(2L, name, email);

        assertThat(user1.hashCode(), not(equalTo(user2.hashCode())));
    }
}