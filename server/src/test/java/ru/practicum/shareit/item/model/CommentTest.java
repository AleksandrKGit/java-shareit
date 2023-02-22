package ru.practicum.shareit.item.model;

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
import static org.hamcrest.Matchers.equalTo;
import static ru.practicum.shareit.tools.factories.ItemFactory.createComment;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentTest {
    final Long id = 1L;
    final String text = "commentText";
    final Item item = createItem(2L, "itemName", "itemDescription", true,
            createUser(3L, "ownerName", "owner@email.com"), null);
    final LocalDateTime created = LocalDateTime.now();
    final User author = createUser(4L, "authorName", "author@email.com");

    @SuppressWarnings("all")
    @Test
    void equals_withSameObjectWithNullIdAndOtherFields_shouldReturnTrue() {
        Comment comment = createComment(null, null, null, null, null);

        assertThat(comment.equals(comment), is(true));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withNullAndNullIdAndOtherFields_shouldReturnFalse() {
        Comment comment = createComment(null, null, null, null, null);

        assertThat(comment.equals(null), is(false));
    }

    @SuppressWarnings("all")
    @Test
    void equals_withObjectOfOtherClassWithNotNullIdAndNullOtherFields_shouldReturnFalse() {
        Comment comment = createComment(id, null, null, null, null);
        User user = createUser(id, null, null);

        assertThat(comment.equals(user), is(false));
    }

    @Test
    void equals_withNullIdsAndNotNullEqualOtherFields_shouldReturnFalse() {
        Comment comment1 = createComment(null, text, created, author, item);
        Comment comment2 = createComment(null, text, created, author, item);

        assertThat(comment1.equals(comment2), is(false));
    }

    @Test
    void equals_withNotNullEqualIdsAndNotEqualOtherFields_shouldReturnTrue() {
        Comment comment1 = createComment(id, null, null, null, null);
        Comment comment2 = createComment(id, text, created, author, item);

        assertThat(comment1.equals(comment2), is(true));
    }

    @ParameterizedTest(name = "ids={0}")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoCommentsWithEqualIdsAndNotEqualOtherFields_shouldBeEqual(Long id) {
        Comment comment1 = createComment(id, text, created, author, item);
        Comment comment2 = createComment(id, null, null, null, null);

        assertThat(comment1.hashCode(), equalTo(comment2.hashCode()));
    }

    @Test
    void hashCode_ofTwoCommentsWithNullAndZeroIdsAndNotEqualOtherFields_shouldBeEqual() {
        Comment comment1 = createComment(null, text, created, author, item);
        Comment comment2 = createComment(0L, null, null, null, null);

        assertThat(comment1.hashCode(), equalTo(comment2.hashCode()));
    }

    @ParameterizedTest(name = "id1={0}, id2=2")
    @NullSource
    @ValueSource(longs = {1L})
    void hashCode_ofTwoCommentsWithNotEqualIdsAndEqualOtherFields_shouldNotBeEqual(Long id) {
        Comment comment1 = createComment(id, text, created, author, item);
        Comment comment2 = createComment(2L, text, created, author, item);

        assertThat(comment1.hashCode(), not(equalTo(comment2.hashCode())));
    }
}