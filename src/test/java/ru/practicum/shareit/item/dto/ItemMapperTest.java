package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItemDtoFromClient;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@SpringBootTest(classes = {ItemMapperImpl.class})
class ItemMapperTest {
    @Autowired
    private ItemMapper itemMapper;

    @Test
    void toDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        Item source = createItem(10L, "itemName", "itemDescription", true, null,
                createItemRequest(20L, null, null, null));

        ItemDtoToClient target = itemMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", is(nullValue())),
                hasProperty("requestId", equalTo(source.getRequest().getId()))
        ));
    }

    @Test
    void toDto_withNull_shouldReturnNull() {
        assertThat(itemMapper.toDto((Item) null), is(nullValue()));
    }

    @Test
    void toDto_withNullFields_shouldReturnDtoWithNullFields() {
        Item source = createItem(null, null, null, null, null, null);

        ItemDtoToClient target = itemMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }

    @Test
    void toBookingDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        Booking source = createBooking(10L, null, null, null, null,
                createUser(20L, null, null));

        BookingDtoToClient target = itemMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("bookerId", equalTo(source.getBooker().getId()))
        ));
    }

    @Test
    void toBookingDto_withNull_shouldReturnNull() {
        assertThat(itemMapper.toDto((Booking) null), is(nullValue()));
    }

    @Test
    void toBookingDto_withNullFields_shouldReturnDtoWithNullFields() {
        Booking source = createBooking(null, null, null, null, null,
                createUser(null, null, null));

        BookingDtoToClient target = itemMapper.toDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("bookerId", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFields() {
        ItemDtoFromClient source = createItemDtoFromClient("itemName", "itemDescription", true,
                20L);

        Item target = itemMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("owner", is(nullValue())),
                hasProperty("request", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(itemMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        ItemDtoFromClient source = createItemDtoFromClient(null, null, null, null);

        Item target = itemMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("owner", is(nullValue())),
                hasProperty("request", is(nullValue()))
        ));
    }

    @Test
    void updateEntityFromDto_withNullFields_shouldNotUpdateEntity() {
        ItemDtoFromClient source = createItemDtoFromClient(null, null, null, null);
        Item target = createItem(10L, "itemName", "itemDescription", true, null,
                createItemRequest(20L, null, null, null));

        itemMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", is(not(nullValue()))),
                hasProperty("description", is(not(nullValue()))),
                hasProperty("available", is(not(nullValue()))),
                hasProperty("request", hasProperty("id", is(not(nullValue()))))
        ));
    }

    @Test
    void updateEntityFromDto_withNotNullFields_shouldUpdateEntity() {
        ItemDtoFromClient source = createItemDtoFromClient("newName", "newDescription", false,
                30L);
        Item target = createItem(10L, "itemName", "itemDescription", true, null,
                createItemRequest(20L, null, null, null));

        itemMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("request",
                        hasProperty("id", is(not(equalTo(source.getRequestId())))))
        ));
    }
}