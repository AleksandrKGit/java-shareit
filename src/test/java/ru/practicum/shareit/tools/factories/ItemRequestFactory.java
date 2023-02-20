package ru.practicum.shareit.tools.factories;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemDtoToClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestFactory {
    public static ItemRequest copyOf(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        ItemRequest copy = new ItemRequest();
        copy.setId(itemRequest.getId());
        copy.setDescription(itemRequest.getDescription());
        copy.setCreated(itemRequest.getCreated());
        copy.setRequestor(UserFactory.copyOf(itemRequest.getRequestor()));
        return copy;
    }

    public static ItemRequest createItemRequest(Long id, String description, LocalDateTime created, User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(id);
        itemRequest.setDescription(description);
        itemRequest.setCreated(created);
        itemRequest.setRequestor(requestor);
        return itemRequest;
    }

    public static ItemRequestDtoFromClient createItemRequestDtoFromClient(String description) {
        ItemRequestDtoFromClient itemRequestDto = new ItemRequestDtoFromClient();
        itemRequestDto.setDescription(description);
        return itemRequestDto;
    }

    public static ItemRequestDtoToClient createItemRequestDtoToClient(Long id, String description,
                                                                      LocalDateTime created,
                                                                      List<ItemDtoToClient> items) {
        ItemRequestDtoToClient itemRequestDto = new ItemRequestDtoToClient();
        itemRequestDto.setId(id);
        itemRequestDto.setCreated(created);
        itemRequestDto.setItems(items);
        itemRequestDto.setDescription(description);
        return itemRequestDto;
    }

    public static ItemDtoToClient createItemDtoToClient(Long id, String name, String description, Boolean available,
                                                        Long requestId) {
        ItemDtoToClient itemDto = new ItemDtoToClient();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }
}
