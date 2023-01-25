package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

public class ItemMapper {
    public static ItemDtoToClient toItemDto(Item item) {
        return item == null ? null : new ItemDtoToClient(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                null
        );
    }

    public static Item toItem(ItemDtoFromClient itemDtoFromClient) {
        return itemDtoFromClient == null ? null : new Item(
                itemDtoFromClient.getId(),
                itemDtoFromClient.getName(),
                itemDtoFromClient.getDescription(),
                itemDtoFromClient.getAvailable(),
                User.builder().id(itemDtoFromClient.getOwnerId()).build(),
                itemDtoFromClient.getRequestId() != null ?
                        ItemRequest.builder().id(itemDtoFromClient.getRequestId()).build() : null
        );
    }

    public static BookingDto toBookingDto(Booking booking) {
        return booking == null ? null : new BookingDto(booking.getId(), booking.getBooker().getId());
    }
}
