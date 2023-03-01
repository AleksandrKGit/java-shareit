package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoToClient;
import java.util.List;

public interface ItemService {
    ItemDtoToClient create(Long ownerId, ItemDtoFromClient dto);

    CommentDtoToClient createComment(Long authorId, Long itemId, CommentDtoFromClient dto);

    List<ItemDtoToClient> readByOwner(Long ownerId, Integer from, Integer size);

    List<ItemDtoToClient> readByQuery(Long userId, String query, Integer from, Integer size);

    ItemDtoToClient readById(Long userId, Long id);

    ItemDtoToClient update(Long ownerId, Long id, ItemDtoFromClient dto);

}
