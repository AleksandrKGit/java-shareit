package ru.practicum.shareit.request;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.common.OffsetPageRequest;
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.user.User;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@Transactional
@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    ItemRequestRepository repository;

    final Long notExistingId = 1000L;

    User requestor;

    User otherUser;

    static final LocalDateTime created = LocalDateTime.now();

    static final String description = "requestDescription";

    static final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void beforeEach() {
        requestor = createUser(null, "requestorName", "requestor@email.com");
        otherUser = createUser(null, "otherUserName", "otherUser@email.com");
        em.persist(requestor);
        em.persist(otherUser);
        em.flush();
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = "requestDescription")
    void save_withWithNullId_shouldReturnAttachedTransferredRequestWithGeneratedId(String description) {
        ItemRequest request = createItemRequest(null, description, created, requestor);

        ItemRequest savedRequest = repository.saveAndFlush(request);

        assertThat(savedRequest == request, is(true));
        assertThat(savedRequest, hasProperty("id", is(not(nullValue()))));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = "desc")
    void save_withWithNotExistingId_shouldReturnNewAttachedRequestWithGeneratedId(String description) {
        ItemRequest request = createItemRequest(notExistingId, description, created, requestor);

        ItemRequest savedRequest = repository.saveAndFlush(request);

        assertThat(request, hasProperty("id", is(notNullValue())));
        assertThat(savedRequest == request, is(false));
        assertThat(savedRequest, allOf(
                hasProperty("id", not(nullValue())),
                hasProperty("id", not(equalTo(request.getId()))),
                hasProperty("description", equalTo(request.getDescription())),
                hasProperty("created", equalTo(request.getCreated())),
                hasProperty("requestor", equalTo(request.getRequestor()))
        ));
    }

    private static Stream<Arguments> incorrectFields() {
        return Stream.of(
                Arguments.of("Entity with null description and not null created",
                        null, created),

                Arguments.of("Entity with big description and not null created",
                        Strings.repeat("d", 256), created),

                Arguments.of("Entity with correct description and null created",
                        description, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectFields")
    void save_withIncorrectFields_shouldThrowException(String testName, String description,
                                                              LocalDateTime created) {
        ItemRequest request = createItemRequest(null, description, created, requestor);

        assertThrows(Exception.class, () -> repository.saveAndFlush(request));
    }

    @Test
    void save_withNullRequestor_shouldThrowException() {
        ItemRequest request = createItemRequest(null, description, created, null);

        assertThrows(Exception.class, () -> repository.saveAndFlush(request));
    }

    @Test
    void save_withDetachedRequestor_shouldThrowException() {
        User requestor = createUser(notExistingId, "newRequestorName", "newRequestor@email.com");
        ItemRequest request = createItemRequest(null, description, created, requestor);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(request));
        assertThat(ConstraintChecker.check(exception, "fk_request_requestor"), is(true));
    }

    @Test
    void findById_withNotExistingId_shouldReturnEmptyOptional() {
        assertThat(repository.findById(notExistingId).isEmpty(), is(true));
    }

    @Test
    void findById_shouldReturnOptionalPresentedByRequestWithSuchId() {
        ItemRequest existingRequest = createItemRequest(null, description, created, requestor);
        em.persist(existingRequest);
        em.flush();
        Long existingId = existingRequest.getId();

        Optional<ItemRequest> result = repository.findById(existingId);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get() == existingRequest, is(true));
    }

    @Test
    void findByRequestor_IdOrderByCreatedDesc_shouldReturnListOfUserRequestsSortedByCreatedDesc() {
        ItemRequest requestCreated1 = createItemRequest(null, "d1", now.minusSeconds(30), requestor);
        ItemRequest requestCreated2OtherUser = createItemRequest(null, "d2", now.minusSeconds(20),
                otherUser);
        ItemRequest requestCreated3 = createItemRequest(null, "d3", now.minusSeconds(10), requestor);
        ItemRequest requestCreated4 = createItemRequest(null, "d4", now.minusSeconds(5), requestor);
        em.persist(requestCreated1);
        em.persist(requestCreated2OtherUser);
        em.persist(requestCreated4);
        em.persist(requestCreated3);
        em.flush();
        Long userId = requestor.getId();

        List<ItemRequest> result = repository.findByRequestor_IdOrderByCreatedDesc(userId);

        assertThat(result, contains(
                equalTo(requestCreated4),
                equalTo(requestCreated3),
                equalTo(requestCreated1)
        ));
    }

    @Test
    void findByRequestor_IdNot_shouldReturnPageOfNotUserRequests() {
        ItemRequest requestCreated1OutOfPage = createItemRequest(null, "d1", now.minusSeconds(50), requestor);
        ItemRequest requestCreated2 = createItemRequest(null, "d2", now.minusSeconds(40), requestor);
        ItemRequest requestCreated3ByUser = createItemRequest(null, "d3", now.minusSeconds(30),
                otherUser);
        ItemRequest requestCreated4 = createItemRequest(null, "d4", now.minusSeconds(20), requestor);
        ItemRequest requestCreated5 = createItemRequest(null, "d5", now.minusSeconds(10), requestor);
        ItemRequest requestCreated6OutOfPage = createItemRequest(null, "d6", now.minusSeconds(5), requestor);
        em.persist(requestCreated1OutOfPage);
        em.persist(requestCreated2);
        em.persist(requestCreated3ByUser);
        em.persist(requestCreated5);
        em.persist(requestCreated4);
        em.persist(requestCreated6OutOfPage);
        em.flush();
        Long userId = otherUser.getId();
        OffsetPageRequest pageRequest = OffsetPageRequest.ofOffset(1, 3,
                Sort.by("created").descending());

        Page<ItemRequest> result = repository.findByRequestor_IdNot(userId, pageRequest);

        assertThat(result, contains(
                equalTo(requestCreated5),
                equalTo(requestCreated4),
                equalTo(requestCreated2)
        ));
    }
}