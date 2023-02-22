package ru.practicum.shareit.request;

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
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.user.User;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.*;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;
import static ru.practicum.shareit.tools.matchers.HamcrestDateMatcher.*;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestControllerIntegrationTests {
    MockMvc mockMvc;

    @Autowired
    ItemRequestController controller;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ControllerErrorHandler controllerErrorHandler;

    final Long notExistingId = 1000L;

    User requestor;

    User otherUser;

    final String description = "requestDescription";

    final LocalDateTime now = LocalDateTime.now();

    final LocalDateTime created = now;

    ItemRequestDtoFromClient requestDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(controllerErrorHandler)
                .build();
        requestor = createUser(null, "requestorName", "requestor@email.com");
        otherUser = createUser(null, "otherUserName", "otherUser@email.com");
        em.persist(requestor);
        em.persist(otherUser);
        em.flush();
        requestDto = createItemRequestDtoFromClient(description);
    }

    @Test
    void create_withNotExistingUserId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", notExistingId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnStatusOkAndDtoOfCreatedRequest() throws Exception {
        Long userId = requestor.getId();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.description", equalTo(requestDto.getDescription())),
                        jsonPath("$.created", near(LocalDateTime.now(), ItemRequestDtoToClient.DATE_PATTERN)));

        ItemRequest createdRequest =
                em.createQuery("Select ir from ItemRequest ir", ItemRequest.class).getSingleResult();

        assertThat(createdRequest, allOf(
                hasProperty("id", is(notNullValue())),
                hasProperty("description", equalTo(requestDto.getDescription())),
                hasProperty("created", greaterThanOrEqualTo(LocalDateTime.now().minusSeconds(2))),
                hasProperty("created", lessThanOrEqualTo(LocalDateTime.now()))
        ));
    }

    @Test
    void readByUser_withNotExistingUserId_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(get("/requests/").header("X-Sharer-User-Id", notExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readByUser_shouldReturnDtoListOfUserRequestsSortedByCreatedDesc() throws Exception {
        ItemRequest requestCreated1 = createItemRequest(null, "d1", now.minusSeconds(30), requestor);
        ItemRequest requestCreated2OtherUser = createItemRequest(null, "d2", now.minusSeconds(20),
                otherUser);
        ItemRequest requestCreated3 = createItemRequest(null, "d3", now.minusSeconds(10), requestor);
        ItemRequest requestCreated4 = createItemRequest(null, "d4", now.minusSeconds(5), requestor);
        em.persist(requestCreated1);
        em.persist(requestCreated2OtherUser);
        em.persist(requestCreated4);
        em.persist(requestCreated3);
        Item request4item = createItem(null, "n1", "d5", true, otherUser,
                requestCreated4);
        Item request3item1 = createItem(null, "n2", "d6", true, otherUser,
                requestCreated3);
        Item request3item3 = createItem(null, "n3", "d7", true, otherUser,
                requestCreated3);
        em.persist(request4item);
        em.persist(request3item1);
        em.persist(request3item3);
        em.flush();
        Long userId = requestor.getId();

        mockMvc.perform(get("/requests/").header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(requestCreated4.getId()), Long.class),
                        jsonPath("$[0].description", equalTo(requestCreated4.getDescription())),
                        jsonPath("$[0].created", equalTo(requestCreated4.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].items[0].id", equalTo(request4item.getId()), Long.class),
                        jsonPath("$[0].items[0].name", equalTo(request4item.getName())),
                        jsonPath("$[0].items[0].description", equalTo(request4item.getDescription())),
                        jsonPath("$[0].items[0].available", equalTo(request4item.getAvailable())),
                        jsonPath("$[0].items[0].requestId", equalTo(requestCreated4.getId()), Long.class),
                        jsonPath("$[1].id", equalTo(requestCreated3.getId()), Long.class),
                        jsonPath("$[1].description", equalTo(requestCreated3.getDescription())),
                        jsonPath("$[1].created", equalTo(requestCreated3.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].items[0].id", equalTo(request3item1.getId()), Long.class),
                        jsonPath("$[1].items[0].name", equalTo(request3item1.getName())),
                        jsonPath("$[1].items[0].description", equalTo(request3item1.getDescription())),
                        jsonPath("$[1].items[0].available", equalTo(request3item1.getAvailable())),
                        jsonPath("$[1].items[0].requestId", equalTo(requestCreated3.getId()), Long.class),
                        jsonPath("$[1].items[1].id", equalTo(request3item3.getId()), Long.class),
                        jsonPath("$[1].items[1].name", equalTo(request3item3.getName())),
                        jsonPath("$[1].items[1].description", equalTo(request3item3.getDescription())),
                        jsonPath("$[1].items[1].available", equalTo(request3item3.getAvailable())),
                        jsonPath("$[1].items[1].requestId", equalTo(requestCreated3.getId()), Long.class),
                        jsonPath("$[2].id", equalTo(requestCreated1.getId()), Long.class),
                        jsonPath("$[2].description", equalTo(requestCreated1.getDescription())),
                        jsonPath("$[2].created", equalTo(requestCreated1.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[2].items", is(empty()))
                );
    }

    @Test
    void readAll_shouldReturnDtoListOfOtherUsersRequestsSortedByCreatedDescWithPagination() throws Exception {
        ItemRequest requestCreated1OutOfPage = createItemRequest(null, "d1", now.minusSeconds(50),
                requestor);
        ItemRequest requestCreated2 = createItemRequest(null, "d2", now.minusSeconds(40), requestor);
        ItemRequest requestCreated3ByUser = createItemRequest(null, "d3", now.minusSeconds(30),
                otherUser);
        ItemRequest requestCreated4 = createItemRequest(null, "d4", now.minusSeconds(20), requestor);
        ItemRequest requestCreated5 = createItemRequest(null, "d5", now.minusSeconds(10), requestor);
        ItemRequest requestCreated6OutOfPage = createItemRequest(null, "d6", now.minusSeconds(5),
                requestor);
        em.persist(requestCreated1OutOfPage);
        em.persist(requestCreated2);
        em.persist(requestCreated3ByUser);
        em.persist(requestCreated5);
        em.persist(requestCreated4);
        em.persist(requestCreated6OutOfPage);
        Item request5Item = createItem(null, "n1", "d7", true, otherUser,
                requestCreated5);
        Item request4Item1 = createItem(null, "n2", "d8", true, otherUser,
                requestCreated4);
        Item request4Item2 = createItem(null, "n3", "d9", true, otherUser,
                requestCreated4);
        em.persist(request5Item);
        em.persist(request4Item1);
        em.persist(request4Item2);
        em.flush();
        Long userId = otherUser.getId();
        int from = 1;
        int size = 3;

        mockMvc.perform(get("/requests/all?from=" + from + "&size=" + size)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(requestCreated5.getId()), Long.class),
                        jsonPath("$[0].description", equalTo(requestCreated5.getDescription())),
                        jsonPath("$[0].created", equalTo(requestCreated5.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].items[0].id", equalTo(request5Item.getId()), Long.class),
                        jsonPath("$[0].items[0].name", equalTo(request5Item.getName())),
                        jsonPath("$[0].items[0].description", equalTo(request5Item.getDescription())),
                        jsonPath("$[0].items[0].available", equalTo(request5Item.getAvailable())),
                        jsonPath("$[0].items[0].requestId", equalTo(requestCreated5.getId()), Long.class),
                        jsonPath("$[1].id", equalTo(requestCreated4.getId()), Long.class),
                        jsonPath("$[1].description", equalTo(requestCreated4.getDescription())),
                        jsonPath("$[1].created", equalTo(requestCreated4.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].items[0].id", equalTo(request4Item1.getId()), Long.class),
                        jsonPath("$[1].items[0].name", equalTo(request4Item1.getName())),
                        jsonPath("$[1].items[0].description", equalTo(request4Item1.getDescription())),
                        jsonPath("$[1].items[0].available", equalTo(request4Item1.getAvailable())),
                        jsonPath("$[1].items[0].requestId", equalTo(requestCreated4.getId()), Long.class),
                        jsonPath("$[1].items[1].id", equalTo(request4Item2.getId()), Long.class),
                        jsonPath("$[1].items[1].name", equalTo(request4Item2.getName())),
                        jsonPath("$[1].items[1].description", equalTo(request4Item2.getDescription())),
                        jsonPath("$[1].items[1].available", equalTo(request4Item2.getAvailable())),
                        jsonPath("$[1].items[1].requestId", equalTo(requestCreated4.getId()), Long.class),
                        jsonPath("$[2].id", equalTo(requestCreated2.getId()), Long.class),
                        jsonPath("$[2].description", equalTo(requestCreated2.getDescription())),
                        jsonPath("$[2].created", equalTo(requestCreated2.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[2].items", is(empty()))
                );
    }

    @Test
    void readById_withNotExistingUserId_shouldReturnStatusNotFound() throws Exception {
        ItemRequest existingRequest = createItemRequest(null, description, created, requestor);
        em.persist(existingRequest);
        em.flush();
        Long existingId = existingRequest.getId();

        mockMvc.perform(get("/requests/" + existingId).header("X-Sharer-User-Id", notExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_withNotExistingRequestId_shouldReturnStatusNotFound() throws Exception {
        Long userId = requestor.getId();

        mockMvc.perform(get("/requests/" + notExistingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_shouldReturnStatusOkAndDtoOfSelectedRequest() throws Exception {
        ItemRequest existingRequest = createItemRequest(null, description, created, requestor);
        em.persist(existingRequest);
        List<Item> items = List.of(
                createItem(null, "n1", "d1", true, otherUser, existingRequest),
                createItem(null, "n2", "d2", false, otherUser, existingRequest)
        );
        items.forEach(em::persist);
        em.flush();
        Long userId = requestor.getId();
        Long existingId = existingRequest.getId();

        mockMvc.perform(get("/requests/" + existingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingRequest.getId()), Long.class),
                        jsonPath("$.description", equalTo(existingRequest.getDescription())),
                        jsonPath("$.created", equalTo(existingRequest.getCreated()
                                .format(DateTimeFormatter.ofPattern(ItemRequestDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.items[0].id", equalTo(items.get(0).getId()), Long.class),
                        jsonPath("$.items[0].name", equalTo(items.get(0).getName())),
                        jsonPath("$.items[0].description", equalTo(items.get(0).getDescription())),
                        jsonPath("$.items[0].available", equalTo(items.get(0).getAvailable())),
                        jsonPath("$.items[0].requestId", equalTo(existingRequest.getId()), Long.class),
                        jsonPath("$.items[1].id", equalTo(items.get(1).getId()), Long.class),
                        jsonPath("$.items[1].name", equalTo(items.get(1).getName())),
                        jsonPath("$.items[1].description", equalTo(items.get(1).getDescription())),
                        jsonPath("$.items[1].available", equalTo(items.get(1).getAvailable())),
                        jsonPath("$.items[1].requestId", equalTo(existingRequest.getId()), Long.class));
    }
}