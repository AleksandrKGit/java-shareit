package ru.practicum.shareit.request;

import ru.practicum.shareit.user.User;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return itemRequest == null ? null : new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return itemRequestDto == null ? null : new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                User.builder().id(itemRequestDto.getRequestorId()).build(),
                null
        );
    }
}