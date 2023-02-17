package ru.practicum.shareit.item.model;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemTest {
    final Long id = 1L;
    final String name = "itemName";
    final String description = "itemDescription";
    final Boolean available = true;
    final User owner = createUser(2L, "ownerName", "owner@email.com");
    final ItemRequest request = createItemRequest(3L, "requestDescription", LocalDateTime.now(),
            createUser(4L, "requestorName", "requestor@email.com"));

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjectWithNullIdAndOtherFields_shouldReturnTrue() {
        Item item = createItem(null, null, null, null, null, null);

        assertThat(item.equals(item), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNullAndNullIdAndOtherFields_shouldReturnFalse() {
        Item item = createItem(null, null, null, null, null, null);

        assertThat(item.equals(null), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withObjectOfOtherClassWithNotNullIdAndNullOtherFields_shouldReturnFalse() {
        Item item = createItem(id, null, null, null, null, null);
        User user = createUser(id, null, null);

        assertThat(item.equals(user), is(false));
    }

    @Test
    void equals_withNullIdsAndNotNullEqualOtherFields_shouldReturnFalse() {
        Item item1 = createItem(null, name, description, available, owner, request);
        Item item2 = createItem(null, name, description, available, owner, request);

        assertThat(item1.equals(item2), is(false));
    }

    @Test
    void equals_withNotNullEqualIdsAndNotEqualOtherFields_shouldReturnTrue() {
        Item item1 = createItem(id, null, null, null, null, null);
        Item item2 = createItem(id, name, description, available, owner, request);

        assertThat(item1.equals(item2), is(true));
    }

    @ParameterizedTest(name = "ids={0}")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoItemsWithEqualIdsAndNotEqualOtherFields_shouldBeEqual(Long id) {
        Item item1 = createItem(id, name, description, available, owner, request);
        Item item2 = createItem(id, null, null, null, null, null);

        assertThat(item1.hashCode(), equalTo(item2.hashCode()));
    }

    @Test
    void hashCode_ofTwoItemsWithNullAndZeroIdsAndNotEqualOtherFields_shouldBeEqual() {
        Item item1 = createItem(null, name, description, available, owner, request);
        Item item2 = createItem(0L, null, null, null, null, null);

        assertThat(item1.hashCode(), equalTo(item2.hashCode()));
    }

    @ParameterizedTest(name = "id1={0}, id2=2")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoItemsWithNotEqualIdsAndEqualOtherFields_shouldNotBeEqual(Long id) {
        Item item1 = createItem(id, name, description, available, owner, request);
        Item item2 = createItem(2L, name, description, available, owner, request);

        assertThat(item1.hashCode(), not(equalTo(item2.hashCode())));
    }
}