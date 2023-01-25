package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoToClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoToClient;
import java.util.Set;

@Service
public interface ItemService {
    ItemDtoToClient create(ItemDtoFromClient itemDtoFromClient);

    ItemDtoToClient readById(Long userId, Long id);

    Set<ItemDtoToClient> readByOwner(Long ownerId);

    Set<ItemDtoToClient> readByQuery(Long userId, String query);

    ItemDtoToClient update(ItemDtoFromClient itemDtoFromClient);

    CommentDtoToClient createComment(CommentDtoFromClient commentDtoFromClient);
}
