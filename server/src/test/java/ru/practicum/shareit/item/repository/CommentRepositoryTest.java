package ru.practicum.shareit.item.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.user.User;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createComment;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@Transactional
@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class CommentRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    CommentRepository repository;

    final Long notExistingId = 1000L;

    User author;

    User otherUser;

    User owner;

    Item item;

    Item otherItem;

    static final String text = "commentText";

    static final LocalDateTime created = LocalDateTime.now();

    @BeforeEach
    void beforeEach() {
        author =  createUser(null, "authorName", "author@email.com");
        otherUser = createUser(null, "otherUserName", "otherUser@email.com");
        owner = createUser(null, "ownerName", "owner@email.com");
        em.persist(author);
        em.persist(otherUser);
        em.persist(owner);
        item = createItem(null, "itemName1", "itemDesc1", true, owner, null);
        otherItem = createItem(null, "itemName2", "itemDesc2", false, owner, null);
        em.persist(item);
        em.persist(otherItem);
        em.flush();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"comment"})
    void save_withWithNullId_shouldReturnAttachedTransferredCommentWithGeneratedId(String text) {
        Comment comment = createComment(null, text, created, author, item);

        Comment savedComment = repository.saveAndFlush(comment);

        assertThat(savedComment == comment, is(true));
        assertThat(savedComment, hasProperty("id", is(not(nullValue()))));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"comment"})
    void save_withWithNotExistingId_shouldReturnNewAttachedCommentWithGeneratedId(String text) {
        Comment comment = createComment(notExistingId, text, created, author, item);

        Comment savedComment = repository.saveAndFlush(comment);

        assertThat(comment, hasProperty("id", is(notNullValue())));
        assertThat(savedComment == comment, is(false));
        assertThat(savedComment, allOf(
                hasProperty("id", not(nullValue())),
                hasProperty("id", not(equalTo(comment.getId()))),
                hasProperty("created", equalTo(comment.getCreated())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("author", equalTo(comment.getAuthor())),
                hasProperty("item", equalTo(comment.getItem()))
        ));
    }

    static Stream<Arguments> incorrectFields() {
        return Stream.of(
                Arguments.of("null text",
                        null, created),

                Arguments.of("null created",
                        text, null),

                Arguments.of("big text",
                        Strings.repeat("n", 2001), created)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectFields")
    void save_withIncorrectFields_shouldThrowException(String testName, String text, LocalDateTime created) {
        Comment comment = createComment(null, text, created, author, item);

        assertThrows(Exception.class, () -> repository.saveAndFlush(comment));
    }

    @Test
    void save_withNullAuthor_shouldThrowException() {
        Comment comment = createComment(null, text, created, null, item);

        assertThrows(Exception.class, () -> repository.saveAndFlush(comment));
    }

    @Test
    void save_withDetachedAuthor_shouldThrowException() {
        User author = createUser(notExistingId, "newAuthorName", "newAuthor@email.com");
        Comment comment = createComment(null, text, created, author, item);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(comment));
        assertThat(ConstraintChecker.check(exception, "fk_comment_author"), is(true));
    }

    @Test
    void save_withNullItem_shouldThrowException() {
        Comment comment = createComment(null, text, created, author, null);

        assertThrows(Exception.class, () -> repository.saveAndFlush(comment));
    }

    @Test
    void save_withDetachedItem_shouldThrowException() {
        Item item = createItem(notExistingId, "newItemName", "newItemDescription", true, owner,
                null);
        Comment comment = createComment(null, text, created, author, item);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(comment));
        assertThat(ConstraintChecker.check(exception, "fk_comment_item"), is(true));
    }

    @Test
    void findById_withNotExistingId_shouldReturnEmptyOptional() {
        assertThat(repository.findById(notExistingId).isEmpty(), is(true));
    }

    @Test
    void findById_withExistingId_shouldReturnOptionalPresentedByCommentWithSuchId() {
        Comment existingComment = createComment(null, text, created, author, item);
        em.persist(existingComment);
        em.flush();
        Long existingId = existingComment.getId();

        Optional<Comment> result = repository.findById(existingId);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get() == existingComment, is(true));
    }

    @Test
    void findByItem_IdOrderByCreatedDesc_shouldReturnListOfItemCommentsSortedByCreatedDesc() {
        Comment commentCreated1 = createComment(null, "text1", created.minusSeconds(10), author, item);
        Comment commentCreated2OtherItem = createComment(null, "text1", created.minusSeconds(20), author, otherItem);
        Comment commentCreated3 = createComment(null, "text1", created.minusSeconds(30), otherUser, item);
        Comment commentCreated4 = createComment(null, "text1", created.minusSeconds(40), author, item);
        em.persist(commentCreated1);
        em.persist(commentCreated2OtherItem);
        em.persist(commentCreated4);
        em.persist(commentCreated3);
        em.flush();
        Long itemId = item.getId();

        List<Comment> result = repository.findByItem_IdOrderByCreatedDesc(itemId);

        assertThat(result, contains(
                commentCreated1,
                commentCreated3,
                commentCreated4
        ));
    }
}