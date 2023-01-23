package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRepositoryInMemory implements ItemRepository {
    Integer id;

    Integer getId() {
        return id++;
    }

    final Map<Integer, Item> items;

    @Autowired
    public ItemRepositoryInMemory() {
        id = 1;
        items = new HashMap<>();
    }

    @Override
    public Item create(Item item) {
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item readById(Integer id) {
        return items.get(id);
    }

    @Override
    public Set<Item> readByOwnerId(Integer ownerId) {
        return items.values().stream().filter(item -> item.getOwner().getId().equals(ownerId))
                .sorted(Comparator.comparingInt(Item::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<Item> readByQuery(String query) {
        return query == null || query.isBlank() ? Set.of() : items.values().stream()
                .filter(item -> item.getAvailable() && (item.getName().toLowerCase().contains(query.toLowerCase())
                        || item.getDescription().toLowerCase().contains(query.toLowerCase())))
                .sorted(Comparator.comparingInt(Item::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Item updateByOwner(Integer ownerId, Item item) {
        Item updatedItem = items.get(item.getId());
        if (updatedItem == null) {
            return null;
        }
        if (!updatedItem.getOwner().getId().equals(ownerId)) {
            return null;
        }
        if (item.getName() != null) {
            updatedItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updatedItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            updatedItem.setAvailable(item.getAvailable());
        }
        return updatedItem;
    }

    @Override
    public void deleteByOwner(Integer ownerId) {
        Set<Integer> removeIds = items.values().stream().filter(item -> item.getOwner().getId().equals(ownerId))
                .map(Item::getId).collect(Collectors.toSet());
        removeIds.forEach(items::remove);
    }
}
