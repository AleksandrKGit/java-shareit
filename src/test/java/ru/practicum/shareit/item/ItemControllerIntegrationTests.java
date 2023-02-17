package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.ControllerErrorHandler;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.user.User;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;
import static ru.practicum.shareit.tools.matchers.HamcrestDateMatcher.near;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemControllerIntegrationTests {
    MockMvc mockMvc;

    @Autowired
    ItemController controller;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ControllerErrorHandler controllerErrorHandler;

    final Long notExistingId = 1000L;

    User owner;

    User otherUser;

    User booker;

    ItemRequest request;

    final String name = "itemName";

    final String description = "itemDescription";

    final Boolean available = true;

    ItemDtoFromClient requestItemDto;

    final LocalDateTime now = LocalDateTime.now();

    final LocalDateTime start = now.minusDays(2);

    final LocalDateTime end = now.minusDays(1);

    CommentDtoFromClient requestCommentDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(controllerErrorHandler)
                .build();
        owner = createUser(null, "owner", "owner@email.com");
        otherUser = createUser(null, "otherUser", "otherUser@email.com");
        User requestor = createUser(null, "requestor", "requestor@email.com");
        booker = createUser(null, "booker", "booker@email.com");
        em.persist(owner);
        em.persist(otherUser);
        em.persist(booker);
        em.persist(requestor);
        request = createItemRequest(null, "requestDescription", now, requestor);
        em.persist(request);
        em.flush();
        requestItemDto = createItemDtoFromClient(name, description, available,null);
        requestCommentDto = createCommentDtoFromClient("commentText");
    }

    @Test
    void create_withNotExistingUserId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", notExistingId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withNotExistingRequestId_shouldReturnStatusNotFound() throws Exception {
        requestItemDto.setRequestId(notExistingId);
        Long userId = owner.getId();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnStatusOkAndDtoOfCreatedItem() throws Exception {
        requestItemDto.setRequestId(request.getId());
        Long userId = owner.getId();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.requestId", equalTo(requestItemDto.getRequestId()), Long.class),
                        jsonPath("$.name", equalTo(requestItemDto.getName())),
                        jsonPath("$.description", equalTo(requestItemDto.getDescription())),
                        jsonPath("$.available", equalTo(requestItemDto.getAvailable())),
                        jsonPath("$.lastBooking", is(nullValue())),
                        jsonPath("$.nextBooking", is(nullValue())),
                        jsonPath("$.comments", nullValue()));

        Item createdItem = em.createQuery("Select i from Item i", Item.class).getSingleResult();

        assertThat(createdItem, allOf(
                hasProperty("id", is(notNullValue())),
                hasProperty("name", equalTo(requestItemDto.getName())),
                hasProperty("description", equalTo(requestItemDto.getDescription())),
                hasProperty("available", equalTo(requestItemDto.getAvailable())),
                hasProperty("owner", equalTo(owner)),
                hasProperty("request", equalTo(request))
        ));
    }

    @Test
    void createComment_withNotBookedItem_shouldReturnStatusBadRequest() throws Exception {
        Item existingItem = createItem(null, name, description, available, owner, null);
        em.persist(existingItem);
        em.flush();
        Long existingId = existingItem.getId();
        Long userId = booker.getId();

        mockMvc.perform(post("/items/" + existingId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isBadRequest());
    }

    @Test
    void createComment_shouldReturnStatusOkAndDtoOfCreatedComment() throws Exception {
        Item existingItem = createItem(null, name, description, available, owner, null);
        em.persist(existingItem);
        Booking booking = createBooking(null, start, end, BookingStatus.APPROVED, existingItem, booker);
        em.persist(booking);
        em.flush();
        Long existingId = existingItem.getId();
        Long userId = booker.getId();

        mockMvc.perform(post("/items/" + existingId + "/comment")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestCommentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.text", equalTo(requestCommentDto.getText())),
                        jsonPath("$.authorName", equalTo(booker.getName())),
                        jsonPath("$.created", near(LocalDateTime.now(), ItemRequestDtoToClient.DATE_PATTERN)));

        Comment target = em.createQuery("Select c from Comment c", Comment.class).getSingleResult();

        assertThat(target, allOf(
                hasProperty("id", is(notNullValue())),
                hasProperty("text", equalTo(requestCommentDto.getText())),
                hasProperty("item", equalTo(existingItem)),
                hasProperty("author", equalTo(booker)),
                hasProperty("created", greaterThanOrEqualTo(LocalDateTime.now().minusSeconds(2))),
                hasProperty("created", lessThanOrEqualTo(LocalDateTime.now()))
        ));
    }

    @Test
    void readByOwner_shouldReturnDtoListOfOwnerItemsSortedByIdAscWithPaginationCommentsNextAndLastBooking()
            throws Exception {
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
        Booking item2BookingNotLast = createBooking(null, start.minusDays(10), end.minusDays(10),
                BookingStatus.APPROVED, itemId2, booker);
        Booking item2BookingLast = createBooking(null, start, end, BookingStatus.APPROVED, itemId2, booker);
        Booking item2BookingNext = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                itemId2, otherUser);
        em.persist(item2BookingLast);
        em.persist(item2BookingNotLast);
        em.persist(item2BookingNext);
        Comment item2Comment1 = createComment(null, "t1", now.minusHours(4), booker, itemId2);
        Comment item2Comment2 = createComment(null, "t2", now.minusHours(5), booker, itemId2);
        em.persist(item2Comment2);
        em.persist(item2Comment1);
        em.flush();
        Long userId = owner.getId();
        int from = 1;
        int size = 2;

        mockMvc.perform(get("/items/?from=" + from + "&size=" + size)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(itemId2.getId()), Long.class),
                        jsonPath("$[0].name", equalTo(itemId2.getName())),
                        jsonPath("$[0].description", equalTo(itemId2.getDescription())),
                        jsonPath("$[0].available", equalTo(itemId2.getAvailable())),
                        jsonPath("$[0].lastBooking.id", equalTo(item2BookingLast.getId()), Long.class),
                        jsonPath("$[0].lastBooking.bookerId",
                                equalTo(item2BookingLast.getBooker().getId()), Long.class),
                        jsonPath("$[0].nextBooking.id", equalTo(item2BookingNext.getId()), Long.class),
                        jsonPath("$[0].nextBooking.bookerId",
                                equalTo(item2BookingNext.getBooker().getId()), Long.class),
                        jsonPath("$[0].comments[0].id", equalTo(item2Comment1.getId()), Long.class),
                        jsonPath("$[0].comments[0].text", equalTo(item2Comment1.getText())),
                        jsonPath("$[0].comments[0].created", equalTo(item2Comment1.getCreated()
                                .format(DateTimeFormatter.ofPattern(CommentDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].comments[0].authorName",
                                equalTo(item2Comment1.getAuthor().getName())),
                        jsonPath("$[0].comments[1].id", equalTo(item2Comment2.getId()), Long.class),
                        jsonPath("$[0].comments[1].text", equalTo(item2Comment2.getText())),
                        jsonPath("$[0].comments[1].created", equalTo(item2Comment2.getCreated()
                                .format(DateTimeFormatter.ofPattern(CommentDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].comments[1].authorName",
                                equalTo(item2Comment2.getAuthor().getName())),
                        jsonPath("$[1].id", equalTo(itemId4.getId()), Long.class),
                        jsonPath("$[1].name", equalTo(itemId4.getName())),
                        jsonPath("$[1].description", equalTo(itemId4.getDescription())),
                        jsonPath("$[1].available", equalTo(itemId4.getAvailable())),
                        jsonPath("$[1].lastBooking", is(nullValue())),
                        jsonPath("$[1].nextBooking", is(nullValue())),
                        jsonPath("$[1].comments", is(empty())));
    }

    @Test
    void readByQuery_shouldReturnDtoListOfAvailableItemsWithQueryInTextOrDescriptionCaseInsensitiveSortedByIdAscWithPaginationAndNextAndLastBookingForOwnerItemsAndWithoutComments()
            throws Exception {
        Item itemId1NotInPage = createItem(null, "Text", "Text", true, owner, request);
        Item itemId2 = createItem(null, "_TeXT_", "d2", true, owner, request);
        Item itemId3NoQuery = createItem(null, "n3", "d3", true, owner, request);
        Item itemId4NotAvailable = createItem(null, "Text", "Text", false, owner, request);
        Item itemId5 = createItem(null, "n5", "teXT1", true, otherUser, request);
        Item itemId7NotInPage = createItem(null, "Text", "Text", true, owner, request);
        em.persist(itemId1NotInPage);
        em.persist(itemId2);
        em.persist(itemId3NoQuery);
        em.persist(itemId4NotAvailable);
        em.persist(itemId5);
        em.persist(itemId7NotInPage);
        Booking item2BookingLast = createBooking(null, start, end, BookingStatus.APPROVED, itemId2, booker);
        Booking item5BookingNextOtherOwner = createBooking(null, now.plusDays(1), now.plusDays(2),
                BookingStatus.WAITING, itemId5, otherUser);
        em.persist(item2BookingLast);
        em.persist(item5BookingNextOtherOwner);
        Comment item2Comment = createComment(null, "t1", now.minusHours(4), booker, itemId2);
        Comment item5Comment = createComment(null, "t2", now.minusHours(5), booker, itemId5);
        em.persist(item2Comment);
        em.persist(item5Comment);
        em.flush();
        Long userId = owner.getId();
        String query = "Text";
        int from = 1;
        int size = 2;

        mockMvc.perform(get("/items/search?text=" + query + "&from=" + from + "&size=" + size)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(itemId2.getId()), Long.class),
                        jsonPath("$[0].name", equalTo(itemId2.getName())),
                        jsonPath("$[0].description", equalTo(itemId2.getDescription())),
                        jsonPath("$[0].available", equalTo(itemId2.getAvailable())),
                        jsonPath("$[0].lastBooking.id", equalTo(item2BookingLast.getId()), Long.class),
                        jsonPath("$[0].lastBooking.bookerId",
                                equalTo(item2BookingLast.getBooker().getId()), Long.class),
                        jsonPath("$[0].nextBooking", is(nullValue())),
                        jsonPath("$[0].comments", is(nullValue())),
                        jsonPath("$[1].id", equalTo(itemId5.getId()), Long.class),
                        jsonPath("$[1].name", equalTo(itemId5.getName())),
                        jsonPath("$[1].description", equalTo(itemId5.getDescription())),
                        jsonPath("$[1].available", equalTo(itemId5.getAvailable())),
                        jsonPath("$[1].lastBooking", is(nullValue())),
                        jsonPath("$[1].nextBooking", is(nullValue())),
                        jsonPath("$[1].comments", is(nullValue())));
    }

    @Test
    void readById_withNotExistingRequestId_shouldReturnStatusNotFound()
            throws Exception {
        Long userId = owner.getId();

        mockMvc.perform(get("/items/" + notExistingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_shouldReturnDtoIfSelectedItemWithCommentsNextAndLastBookingForOwner()
            throws Exception {
        Item existingItem = createItem(null, "itemName", "itemDescription", true,
                owner, request);
        em.persist(existingItem);
        Booking bookingNotLast = createBooking(null, start.minusDays(10), end.minusDays(10),
                BookingStatus.APPROVED, existingItem, booker);
        Booking bookingLast = createBooking(null, start, end, BookingStatus.APPROVED, existingItem, booker);
        Booking bookingNext = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                existingItem, otherUser);
        em.persist(bookingLast);
        em.persist(bookingNotLast);
        em.persist(bookingNext);
        Comment comment1 = createComment(null, "t1", now.minusHours(4), booker, existingItem);
        Comment comment2 = createComment(null, "t2", now.minusHours(5), booker, existingItem);
        em.persist(comment2);
        em.persist(comment1);
        em.flush();
        Long existingId = existingItem.getId();
        Long userId = owner.getId();

        mockMvc.perform(get("/items/" + existingId).header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingItem.getId()), Long.class),
                        jsonPath("$.name", equalTo(existingItem.getName())),
                        jsonPath("$.description", equalTo(existingItem.getDescription())),
                        jsonPath("$.available", equalTo(existingItem.getAvailable())),
                        jsonPath("$.lastBooking.id", equalTo(bookingLast.getId()), Long.class),
                        jsonPath("$.lastBooking.bookerId",
                                equalTo(bookingLast.getBooker().getId()), Long.class),
                        jsonPath("$.nextBooking.id", equalTo(bookingNext.getId()), Long.class),
                        jsonPath("$.nextBooking.bookerId",
                                equalTo(bookingNext.getBooker().getId()), Long.class),
                        jsonPath("$.comments[0].id", equalTo(comment1.getId()), Long.class),
                        jsonPath("$.comments[0].text", equalTo(comment1.getText())),
                        jsonPath("$.comments[0].created", equalTo(comment1.getCreated()
                                .format(DateTimeFormatter.ofPattern(CommentDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.comments[0].authorName",
                                equalTo(comment1.getAuthor().getName())),
                        jsonPath("$.comments[1].id", equalTo(comment2.getId()), Long.class),
                        jsonPath("$.comments[1].text", equalTo(comment2.getText())),
                        jsonPath("$.comments[1].created", equalTo(comment2.getCreated()
                                .format(DateTimeFormatter.ofPattern(CommentDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.comments[1].authorName",
                                equalTo(comment2.getAuthor().getName())));
    }

    @Test
    void update_withNotExistingRequestId_shouldReturnStatusNotFound() throws Exception {
        Long userId = owner.getId();

        mockMvc.perform(patch("/items/" + notExistingId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound());
    }

    @Test
    void update_withNotOwnerItem_shouldReturnStatusForbidden() throws Exception {
        Item existingItem = createItem(null, "itemName", "itemDescription", true,
                otherUser, request);
        em.persist(existingItem);
        em.flush();
        Long existingId = existingItem.getId();
        Long userId = owner.getId();

        mockMvc.perform(patch("/items/" + existingId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isForbidden());
    }

    @Test
    void update_shouldNotUpdateItemRequestAndShouldReturnStatusOkAndDtoOfUpdatedItemWithLastAndNextBookingAndWithoutComments()
            throws Exception {
        Item existingItem = createItem(null, "oldName", "oldDescription", false,
                owner, request);
        em.persist(existingItem);
        em.flush();
        Booking bookingLast = createBooking(null, start, end, BookingStatus.APPROVED, existingItem, booker);
        Booking bookingNext = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                existingItem, otherUser);
        em.persist(bookingLast);
        em.persist(bookingNext);
        Comment comment = createComment(null, "t1", now.minusHours(4), booker, existingItem);
        em.persist(comment);
        em.flush();
        Long existingId = existingItem.getId();
        Long userId = owner.getId();

        mockMvc.perform(patch("/items/" + existingId)
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestItemDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingItem.getId()), Long.class),
                        jsonPath("$.name", equalTo(existingItem.getName())),
                        jsonPath("$.description", equalTo(existingItem.getDescription())),
                        jsonPath("$.available", equalTo(existingItem.getAvailable())),
                        jsonPath("$.lastBooking.id", equalTo(bookingLast.getId()), Long.class),
                        jsonPath("$.lastBooking.bookerId",
                                equalTo(bookingLast.getBooker().getId()), Long.class),
                        jsonPath("$.nextBooking.id", equalTo(bookingNext.getId()), Long.class),
                        jsonPath("$.nextBooking.bookerId",
                                equalTo(bookingNext.getBooker().getId()), Long.class),
                        jsonPath("$.comments", is(nullValue())));

        assertThat(em.contains(existingItem), is(true));
        assertThat(existingItem, allOf(
                hasProperty("id", equalTo(existingId)),
                hasProperty("name", equalTo(requestItemDto.getName())),
                hasProperty("description", equalTo(requestItemDto.getDescription())),
                hasProperty("available", equalTo(requestItemDto.getAvailable())),
                hasProperty("request", is(not(nullValue())))
        ));
    }
}