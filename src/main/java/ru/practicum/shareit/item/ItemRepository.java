package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface ItemRepository {
    Item create(Item item);
    Item readById(Integer id);
    Set<Item> readByOwnerId(Integer ownerId);
    Set<Item> readByQuery(String query);
    Item updateByOwner(Integer ownerId, Item item);
    void deleteByOwner(Integer ownerId);
}