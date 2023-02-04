package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoToClient;
import java.util.Set;

public interface ItemService {
    ItemDtoToClient create(Long ownerId, ItemDtoFromClient itemDtoFromClient);

    ItemDtoToClient readById(Long userId, Long id);

    Set<ItemDtoToClient> readByOwner(Long ownerId);

    Set<ItemDtoToClient> readByQuery(Long userId, String query);

    ItemDtoToClient update(Long ownerId, Long id, ItemDtoFromClient itemDtoFromClient);

    CommentDtoToClient createComment(Long authorId, Long itemId, CommentDtoFromClient commentDtoFromClient);
}
