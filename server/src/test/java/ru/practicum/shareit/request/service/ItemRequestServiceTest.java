package ru.practicum.shareit.request.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.request.dto.ItemRequestMapperImpl;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.tools.factories.ItemFactory;
import ru.practicum.shareit.tools.factories.UserFactory;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.*;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@SpringBootTest(classes = {ItemRequestServiceImpl.class, ItemRequestMapperImpl.class})
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestServiceTest {
    @Autowired
    ItemRequestService service;

    @MockBean
    UserRepository userRepository;

    @MockBean
    ItemRepository itemRepository;

    @MockBean
    ItemRequestRepository repository;

    ItemRequestDtoFromClient requestDto;

    final Long id = 1L;

    ItemRequest createdRequest;

    ItemRequest existingRequest;

    final Long userId = 2L;

    User user;

    Item item;

    @BeforeEach
    void setUp() {
        user = createUser(userId, null, null);
        requestDto = createItemRequestDtoFromClient("requestDescription");
        createdRequest = createItemRequest(id, requestDto.getDescription(), LocalDateTime.now(), user);
        existingRequest = createItemRequest(id, "d2", LocalDateTime.now().minusDays(1), user);
        item = createItem(4L, "itemName", "itemDescription", true, null, createdRequest);
    }

    @Test
    void create_withNotExistingUser_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(userId, requestDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldReturnDtoOfCreatedRequest() {
        ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor = ArgumentCaptor.forClass(ItemRequest.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.saveAndFlush(itemRequestArgumentCaptor.capture())).thenReturn(copyOf(createdRequest));

        ItemRequestDtoToClient resultRequestDto = service.create(userId, requestDto);
        ItemRequest requestToRepository = itemRequestArgumentCaptor.getValue();

        assertThat(requestToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("description", equalTo(requestDto.getDescription())),
                hasProperty("created", lessThanOrEqualTo(LocalDateTime.now().plusSeconds(2))),
                hasProperty("created", greaterThan(LocalDateTime.now().minusSeconds(2))),
                hasProperty("requestor", hasProperty("id", equalTo(user.getId())))
        ));

        assertThat(resultRequestDto, allOf(
                hasProperty("id", equalTo(createdRequest.getId())),
                hasProperty("description", equalTo(createdRequest.getDescription())),
                hasProperty("created", equalTo(createdRequest.getCreated())),
                hasProperty("items", is(nullValue()))
        ));
    }

    @Test
    void readById_withNotExistingUser_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readById(userId, id));
    }

    @Test
    void readById_withNotExistingId_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readById(userId, id));
    }

    @Test
    void readById_shouldReturnDtoOfSelectedRequestWithItems() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingRequest)));
        when(itemRepository.findByRequest_IdOrderByIdAsc(id)).thenReturn(List.of(ItemFactory.copyOf(item)));

        ItemRequestDtoToClient resultRequestDto = service.readById(userId, id);

        assertThat(resultRequestDto, allOf(
                hasProperty("id", equalTo(existingRequest.getId())),
                hasProperty("description", equalTo(existingRequest.getDescription())),
                hasProperty("created", equalTo(existingRequest.getCreated())),
                hasProperty("items", contains(allOf(
                        hasProperty("id", equalTo(item.getId())),
                        hasProperty("name", equalTo(item.getName())),
                        hasProperty("description", equalTo(item.getDescription())),
                        hasProperty("available", equalTo(item.getAvailable())),
                        hasProperty("requestId", equalTo(item.getRequest().getId()))
                )))
        ));
    }

    @Test
    void readByUser_withNotExistingUser_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readByUser(userId));
    }

    @Test
    void readByUser_shouldReturnDtoListOfUserRequestsWithItems() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.findByRequestor_IdOrderByCreatedDesc(userId)).thenReturn(List.of(copyOf(existingRequest)));
        when(itemRepository.findByRequest_IdOrderByIdAsc(existingRequest.getId()))
                .thenReturn(List.of(ItemFactory.copyOf(item)));

        List<ItemRequestDtoToClient> resultRequestDtoList = service.readByUser(userId);

        assertThat(resultRequestDtoList, contains(allOf(
                hasProperty("id", equalTo(existingRequest.getId())),
                hasProperty("description", equalTo(existingRequest.getDescription())),
                hasProperty("created", equalTo(existingRequest.getCreated())),
                hasProperty("items", contains(allOf(
                        hasProperty("id", equalTo(item.getId())),
                        hasProperty("name", equalTo(item.getName())),
                        hasProperty("description", equalTo(item.getDescription())),
                        hasProperty("available", equalTo(item.getAvailable())),
                        hasProperty("requestId", equalTo(item.getRequest().getId()))
                )))
        )));
    }

    @Test
    void readAll_shouldReturnDtoListFromPageOfAllRequestsWithItems() {
        Integer from = 2;
        Integer size = 5;
        OffsetPageRequest pageRequest =
                OffsetPageRequest.ofOffset(from, size, Sort.by("created").descending());
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.findByRequestor_IdNot(userId, pageRequest))
                .thenReturn(new PageImpl<>(List.of(copyOf(existingRequest))));
        when(itemRepository.findByRequest_IdOrderByIdAsc(existingRequest.getId())).thenReturn(List.of(ItemFactory.copyOf(item)));

        List<ItemRequestDtoToClient> resultRequestDtoList = service.readAll(userId, from, size);

        assertThat(resultRequestDtoList, contains(allOf(
                hasProperty("id", equalTo(existingRequest.getId())),
                hasProperty("description", equalTo(existingRequest.getDescription())),
                hasProperty("created", equalTo(existingRequest.getCreated())),
                hasProperty("items", contains(allOf(
                        hasProperty("id", equalTo(item.getId())),
                        hasProperty("name", equalTo(item.getName())),
                        hasProperty("description", equalTo(item.getDescription())),
                        hasProperty("available", equalTo(item.getAvailable())),
                        hasProperty("requestId", equalTo(item.getRequest().getId()))
                )))
        )));
    }
}