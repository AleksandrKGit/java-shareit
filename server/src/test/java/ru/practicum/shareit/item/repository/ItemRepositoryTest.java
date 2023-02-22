package ru.practicum.shareit.item.repository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
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
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@Transactional
@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    ItemRepository repository;

    final Long notExistingId = 1000L;

    User owner;

    User otherUser;

    User requestor;

    ItemRequest request;

    ItemRequest otherRequest;

    static final String name = "itemName";

    static final String description = "itemDescription";

    final LocalDateTime now = LocalDateTime.now();

    final Sort sort = Sort.by("id").ascending();

    @BeforeEach
    void beforeEach() {
        owner = createUser(null, "ownerName", "owner@email.com");
        requestor =  createUser(null, "requestorName", "requestor@email.com");
        otherUser = createUser(null, "otherUserName", "other@email.com");
        em.persist(owner);
        em.persist(requestor);
        em.persist(otherUser);
        request = createItemRequest(null, "requestDescription", now, requestor);
        otherRequest = createItemRequest(null, "otherRequestDescription", now.minusDays(1), requestor);
        em.persist(request);
        em.persist(otherRequest);
        em.flush();
    }

    static Stream<Arguments> save() {
        return Stream.of(
                Arguments.of("", "", false, false),
                Arguments.of(name, description, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource("save")
    void save_withWithNullId_shouldReturnAttachedTransferredItemWithGeneratedId(
            String name, String description, boolean available, boolean withRequest) {
        Item item = createItem(null, name, description, available, owner, withRequest ? request : null);

        Item savedItem = repository.saveAndFlush(item);

        assertThat(savedItem == item, is(true));
        assertThat(savedItem, hasProperty("id", is(not(nullValue()))));
    }

    @ParameterizedTest
    @MethodSource("save")
    void save_withWithNotExistingId_shouldReturnNewAttachedItemWithGeneratedId(
            String name, String description, boolean available, boolean withRequest) {
        Item item = createItem(notExistingId, name, description, available, owner, withRequest ? request : null);

        Item savedItem = repository.saveAndFlush(item);

        assertThat(item, hasProperty("id", is(notNullValue())));
        assertThat(savedItem == item, is(false));
        assertThat(savedItem, allOf(
                hasProperty("id", not(nullValue())),
                hasProperty("id", not(equalTo(item.getId()))),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable())),
                hasProperty("request", equalTo(item.getRequest())),
                hasProperty("owner", equalTo(item.getOwner()))
        ));
    }

    static Stream<Arguments> incorrectFields() {
        return Stream.of(
                Arguments.of("null name",
                        null, description, false),

                Arguments.of("null description",
                        name, null, false),

                Arguments.of("null available",
                        name, description, null),

                Arguments.of("big name",
                        Strings.repeat("n", 256), description, false),

                Arguments.of("big description",
                        name, Strings.repeat("n", 2001), false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectFields")
    void save_withIncorrectFields_shouldThrowException(String testName, String name, String description,
                                                              Boolean available) {
        Item item = createItem(null, name, description, available, owner, null);

        assertThrows(Exception.class, () -> repository.saveAndFlush(item));
    }

    @Test
    void save_withNullOwner_shouldThrowException() {
        Item item = createItem(null, name, description, true, null, null);

        assertThrows(Exception.class, () -> repository.saveAndFlush(item));
    }

    @Test
    void save_withDetachedOwner_shouldThrowException() {
        User owner = createUser(notExistingId, "newOwnerName", "newOwner@email.com");
        Item item = createItem(null, name, description, true, owner, null);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(item));
        assertThat(ConstraintChecker.check(exception, "fk_item_owner"), is(true));
    }

    @Test
    void save_withDetachedRequest_shouldThrowException() {
        ItemRequest request = createItemRequest(notExistingId, "newDescription", now, requestor);
        Item item = createItem(null, name, description, true, owner, request);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(item));
        assertThat(ConstraintChecker.check(exception, "fk_item_request"), is(true));
    }

    static Stream<Arguments> update() {
        return Stream.of(
                Arguments.of("", "", false),
                Arguments.of(name, description, true)
        );
    }

    @ParameterizedTest(name = "nm=n,dn=d,al=false => nm={0},dn={1},al={2}")
    @MethodSource("update")
    void update_shouldUpdateItemFields(String name, String description, boolean available) {
        Item existingItem = createItem(null, "n", "d", false, owner,
                request);
        em.persist(existingItem);
        em.flush();
        Long existingId = existingItem.getId();
        Item item = createItem(existingId, name, description, available, owner, request);

        Item updatedItem = repository.saveAndFlush(item);

        assertThat(updatedItem == existingItem, is(true));
        assertThat(updatedItem == item, is(false));
        assertThat(updatedItem, allOf(
                hasProperty("id", equalTo(existingId)),
                hasProperty("name", equalTo(item.getName())),
                hasProperty("description", equalTo(item.getDescription())),
                hasProperty("available", equalTo(item.getAvailable())),
                hasProperty("request", equalTo(item.getRequest())),
                hasProperty("owner", equalTo(item.getOwner()))
        ));
    }

    @Test
    void findById_withNotExistingId_shouldReturnEmptyOptional() {
        assertThat(repository.findById(notExistingId).isEmpty(), is(true));
    }

    @Test
    void findById_shouldReturnOptionalPresentedByItemWithSuchId() {
        Item item = createItem(null, name, description, true, owner, null);
        em.persist(item);
        em.flush();
        Long existingId = item.getId();

        Optional<Item> result = repository.findById(existingId);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get() == item, is(true));
    }

    @Test
    void findByOwner_Id_shouldReturnPageOfUserItems() {
        Item itemId1NotInPage = createItem(null, "n1", "d1", true, owner, null);
        Item itemId2 = createItem(null, "n3", "d3", true, owner, null);
        Item itemId3NotOwner = createItem(null, "n2", "d2", true, otherUser, null);
        Item itemId4 = createItem(null, "n4", "d4", true, owner, null);
        Item itemId5NotInPage = createItem(null, "n5", "d5", true, owner, null);
        em.persist(itemId1NotInPage);
        em.persist(itemId2);
        em.persist(itemId3NotOwner);
        em.persist(itemId4);
        em.persist(itemId5NotInPage);
        em.flush();
        Long userId = owner.getId();
        OffsetPageRequest pageRequest = OffsetPageRequest.ofOffset(1, 2, sort);

        Page<Item> result = repository.findByOwner_Id(userId, pageRequest);

        assertThat(result, contains(
                itemId2,
                itemId4
        ));
    }

    @Test
    void findByRequest_IdOrderByIdAsc_shouldReturnListOfItemsCreatedFromRequestSortedByIdAsc() {
        Item itemId1 = createItem(null, "n1", "d1", true, owner, request);
        Item itemId2NullRequest = createItem(null, "n2", "d2", true, owner, null);
        Item itemId3OtherRequest = createItem(null, "n3", "d3", true, owner, otherRequest);
        Item itemId4 = createItem(null, "n4", "d4", true, otherUser, request);
        em.persist(itemId1);
        em.persist(itemId2NullRequest);
        em.persist(itemId3OtherRequest);
        em.persist(itemId4);
        em.flush();
        Long requestId = request.getId();

        List<Item> result = repository.findByRequest_IdOrderByIdAsc(requestId);

        assertThat(result, contains(
                itemId1,
                itemId4
        ));
    }

    @Test
    void findByQuery_shouldReturnReturnPageOfAvailableItemsWithQueryInTextOrDescriptionCaseInsensitive() {
        Item itemId1NotInPage = createItem(null, "Text", "Text", true, owner, request);
        Item itemId2 = createItem(null, "teXT1", "d2", true, owner, request);
        Item itemId3 = createItem(null, "someTExt", "d3", true, owner, otherRequest);
        Item itemId4 = createItem(null, "_TEXT_", "d4", true, owner, null);
        Item itemId5NoQuery = createItem(null, "n5", "d5", true, owner, request);
        Item itemId6NotAvailable = createItem(null, "Text", "Text", false, owner, request);
        Item itemId7 = createItem(null, "n7", "teXT1", true, otherUser, request);
        Item itemId8 = createItem(null, "n8", "someTExt", true, otherUser, otherRequest);
        Item itemId9 = createItem(null, "n9", "_TEXT_", true, otherUser, null);
        Item itemId10NotInPage = createItem(null, "Text", "Text", true, owner, request);
        em.persist(itemId1NotInPage);
        em.persist(itemId2);
        em.persist(itemId3);
        em.persist(itemId4);
        em.persist(itemId5NoQuery);
        em.persist(itemId6NotAvailable);
        em.persist(itemId7);
        em.persist(itemId8);
        em.persist(itemId9);
        em.persist(itemId10NotInPage);
        em.flush();
        OffsetPageRequest pageRequest = OffsetPageRequest.ofOffset(1, 6, sort);
        String query = "Text";

        Page<Item> result = repository.findByQuery(query, pageRequest);

        assertThat(result, contains(
                itemId2,
                itemId3,
                itemId4,
                itemId7,
                itemId8,
                itemId9
        ));
    }
}