package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;
import ru.practicum.shareit.request.dto.ItemRequestDtoToClient;
import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoToClient create(Long userId, ItemRequestDtoFromClient dto);

    List<ItemRequestDtoToClient> readByUser(Long userId);

    List<ItemRequestDtoToClient> readAll(Long userId, Integer from, Integer size);

    ItemRequestDtoToClient readById(Long userId, Long id);
}
