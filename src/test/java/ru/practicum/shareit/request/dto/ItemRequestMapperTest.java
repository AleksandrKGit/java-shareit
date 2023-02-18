package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.*;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@SpringBootTest(classes = {ItemRequestMapperImpl.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemRequestMapperTest {
    @Autowired
    ItemRequestMapper itemRequestMapper;

    ItemRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(ItemRepository.class);
    }

    @Test
    void toDto_withNotNullFieldsAndRepository_shouldReturnDtoWithNotNullFields() {
        ItemRequest source = createItemRequest(10L, "name", LocalDateTime.now(),
                createUser(1000L, "name", "email"));
        List<Item> items = List.of(createItem(2L, "itemName", "itemDesc", true, null,
                source));
        when(repository.findByRequest_IdOrderByIdAsc(source.getId())).thenReturn(items);

        ItemRequestDtoToClient target = itemRequestMapper.toDto(source, repository);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("created", equalTo(source.getCreated())),
                hasProperty("items", contains(allOf(
                        hasProperty("id", equalTo(items.get(0).getId())),
                        hasProperty("name", equalTo(items.get(0).getName())),
                        hasProperty("description", equalTo(items.get(0).getDescription())),
                        hasProperty("available", equalTo(items.get(0).getAvailable())),
                        hasProperty("requestId", equalTo(items.get(0).getRequest().getId()))
                )))
        ));
    }

    @Test
    void toDto_withNullAndNullRepository_shouldReturnNull() {
        assertThat(itemRequestMapper.toDto(null, null), is(nullValue()));
    }

    @Test
    void toDto_withNullFieldsAndRepository_shouldReturnDtoWithNullFields() {
        ItemRequest source = createItemRequest(null, null, null, null);

        ItemRequestDtoToClient target = itemRequestMapper.toDto(source, null);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("created", is(nullValue())),
                hasProperty("items", is(nullValue()))
        ));
    }

    @Test
    void toItemDto_withNotNullFields_shouldReturnItemDtoWithNotNullFields() {
        Item source = createItem(10L, "name", "desc", true,
                createUser(20L, "owner", "owner@email.com"),
                createItemRequest(30L, "name", LocalDateTime.now(),
                        createUser(40L, "requestor", "requestor@email.com")));

        ItemDtoToClient target = itemRequestMapper.toItemDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("requestId", equalTo(source.getRequest().getId()))
        ));
    }

    @Test
    void toItemDto_withNull_shouldReturnNull() {
        assertThat(itemRequestMapper.toItemDto(null), is(nullValue()));
    }

    @Test
    void toItemDto_withNullFieldsExceptRequestWithNullId_shouldReturnDtoWithNullFields() {
        Item source = createItem(null, null, null, null, null,
                createItemRequest(null, "description", LocalDateTime.now(),
                        createUser(1L, "name", "info@email.com")));

        ItemDtoToClient target = itemRequestMapper.toItemDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }

    @Test
    void toItemDto_withNullFields_shouldReturnDtoWithNullFields() {
        Item source = createItem(null, null, null, null, null, null);

        ItemDtoToClient target = itemRequestMapper.toItemDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFields() {
        ItemRequestDtoFromClient source = createItemRequestDtoFromClient("name");

        ItemRequest target = itemRequestMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("created", is(nullValue())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("requestor", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(itemRequestMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        ItemRequestDtoFromClient source = createItemRequestDtoFromClient(null);

        ItemRequest target = itemRequestMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("created", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("requestor", is(nullValue()))
        ));
    }
}