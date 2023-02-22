package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.practicum.shareit.support.ConstraintChecker;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static ru.practicum.shareit.tools.factories.UserFactory.*;

@Transactional
@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    UserRepository repository;

    static final Long notExistingId = 1000L;
    static final String name = "userName";
    static final String email = "user@email.com";

    private static Stream<Arguments> save() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of(name, email)
        );
    }

    @ParameterizedTest(name = "name={0}, email={1}")
    @MethodSource("save")
    void save_withWithNullId_shouldReturnAttachedTransferredUserWithGeneratedId(String name, String email) {
        User user = createUser(null, name, email);

        User savedUser = repository.saveAndFlush(user);

        assertThat(savedUser == user, is(true));
        assertThat(savedUser, hasProperty("id", is(not(nullValue()))));
    }

    @ParameterizedTest(name = "name={0}, email={1}")
    @MethodSource("save")
    void save_withWithNotExistingId_shouldReturnNewAttachedUserWithGeneratedId(String name, String email) {
        User user = createUser(notExistingId, name, email);

        User savedUser = repository.saveAndFlush(user);

        assertThat(user, hasProperty("id", is(notNullValue())));
        assertThat(savedUser == user, is(false));
        assertThat(savedUser, allOf(
                hasProperty("id", not(nullValue())),
                hasProperty("id", not(equalTo(user.getId()))),
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    private static Stream<Arguments> incorrectFields() {
        return Stream.of(
                Arguments.of("null name",
                        null, email),

                Arguments.of("big name",
                        Strings.repeat("n", 256), email),

                Arguments.of("null email",
                        name, null),

                Arguments.of("big email",
                        name, Strings.repeat("n", 256))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectFields")
    void save_withIncorrectFields_shouldThrowException(String testName, String name, String email) {
        User user = createUser(null, name, email);

        assertThrows(Exception.class, () -> repository.saveAndFlush(user));
    }

    @Test
    void save_withNotUniqueEmail_shouldThrowExceptionWithEmailConstraint() {
        User existingUser = createUser(null, "existingUserName", email);
        em.persist(existingUser);
        User user = createUser(null, name, email);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(user));
        assertThat(ConstraintChecker.check(exception, "uq_user_email"), is(true));
    }

    private static Stream<Arguments> update() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of(name, email),
                Arguments.of("newName", "new@email.com")
        );
    }

    @ParameterizedTest(name = "name=oldName, email=old@email.com => name={0}, email={1}")
    @MethodSource("update")
    void update_shouldUpdateUserFields(String name, String email) {
        User existingUser = createUser(null, "oldName", "old@email.com");
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();
        User user = createUser(existingId, name, email);

        User updatedUser = repository.saveAndFlush(user);

        assertThat(updatedUser == existingUser, is(true));
        assertThat(updatedUser == user, is(false));
        assertThat(updatedUser, allOf(
                hasProperty("id", equalTo(existingId)),
                hasProperty("name", equalTo(user.getName())),
                hasProperty("email", equalTo(user.getEmail()))
        ));
    }

    @Test
    void findAll_shouldReturnListWithAllUsersInAnyOrder() {
        User user1 = createUser(null, "n1", "e1@email.com");
        User user2 = createUser(null, "n2", "e2@email.com");
        em.persist(user1);
        em.persist(user2);

        List<User> result = repository.findAll();

        assertThat(result, containsInAnyOrder(
                user1,
                user2
        ));
    }

    @Test
    void delete_withNotExistingId_shouldThrowEmptyResultDataAccessException() {
        assertThrows(EmptyResultDataAccessException.class, () -> repository.deleteById(notExistingId));
    }

    @Test
    void delete_shouldRemoveUserWithSuchId() {
        User existingUser = createUser(null, name, email);
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();

        repository.deleteById(existingId);

        assertThat(repository.findById(existingId).isEmpty(), is(true));
    }

    @Test
    void findById_withNotExistingId_shouldReturnEmptyOptional() {
        assertThat(repository.findById(notExistingId).isEmpty(), is(true));
    }

    @Test
    void findById_shouldReturnOptionalPresentedByUserWithSuchId() {
        User existingUser = createUser(null, name, email);
        em.persist(existingUser);
        em.flush();
        Long existingId = existingUser.getId();

        Optional<User> result = repository.findById(existingId);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get() == existingUser, is(true));
    }
}