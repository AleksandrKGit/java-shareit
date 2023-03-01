package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestTest {
    final Long id = 1L;
    final User requestor = createUser(2L, "name", "email@email.com");
    final LocalDateTime created = LocalDateTime.now();
    final String description = "desc";

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjectWithNullIdAndOtherFields_shouldReturnTrue() {
        ItemRequest itemRequest = createItemRequest(null, null, null, null);

        assertThat(itemRequest.equals(itemRequest), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNullAndNullIdAndOtherFields_shouldReturnFalse() {
        ItemRequest itemRequest = createItemRequest(null, null, null, null);

        assertThat(itemRequest.equals(null), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withObjectOfOtherClassWithNotNullIdAndNullOtherFields_shouldReturnFalse() {
        ItemRequest itemRequest = createItemRequest(id, null, null, null);
        User user = createUser(id, null, null);

        assertThat(itemRequest.equals(user), is(false));
    }

    @Test
    void equals_withNullIdsAndNotNullEqualOtherFields_shouldReturnFalse() {
        ItemRequest itemRequest1 = createItemRequest(null, description, created, requestor);
        ItemRequest itemRequest2 = createItemRequest(null, description, created, requestor);

        assertThat(itemRequest1.equals(itemRequest2), is(false));
    }

    @Test
    void equals_withNotNullEqualIdsAndNotEqualOtherFields_shouldReturnTrue() {
        ItemRequest itemRequest1 = createItemRequest(id, null, null, null);
        ItemRequest itemRequest2 = createItemRequest(id, description, created, requestor);

        assertThat(itemRequest1.equals(itemRequest2), is(true));
    }

    @ParameterizedTest(name = "ids={0}")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoItemRequestsWithEqualIdsAndNotEqualOtherFields_shouldBeEqual(Long id) {
        ItemRequest itemRequest1 = createItemRequest(id, description, created, requestor);
        ItemRequest itemRequest2 = createItemRequest(id, null, null, null);

        assertThat(itemRequest1.hashCode(), equalTo(itemRequest2.hashCode()));
    }

    @Test
    void hashCode_ofTwoItemRequestsWithNullAndZeroIdsAndNotEqualOtherFields_shouldBeEqual() {
        ItemRequest itemRequest1 = createItemRequest(null, description, created, requestor);
        ItemRequest itemRequest2 = createItemRequest(0L, null, null, null);

        assertThat(itemRequest1.hashCode(), equalTo(itemRequest2.hashCode()));
    }

    @ParameterizedTest(name = "id1={0}, id2=2")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoItemRequestsWithNotEqualIdsAndEqualOtherFields_shouldNotBeEqual(Long id) {
        ItemRequest itemRequest1 = createItemRequest(id, description, created, requestor);
        ItemRequest itemRequest2 = createItemRequest(2L, description, created, requestor);

        assertThat(itemRequest1.hashCode(), not(equalTo(itemRequest2.hashCode())));
    }
}