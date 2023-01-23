package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface ItemService {
    ItemDto create(ItemDto itemDto);

    ItemDto readById(Integer id);

    Set<ItemDto> readByOwner(Integer ownerId);

    Set<ItemDto> readByQuery(String query);

    ItemDto update(ItemDto itemDto);
}
