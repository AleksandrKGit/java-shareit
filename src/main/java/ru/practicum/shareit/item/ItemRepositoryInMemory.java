package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
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

    final DefaultLocaleMessageSource messageSource;

    @Autowired
    public ItemRepositoryInMemory(DefaultLocaleMessageSource messageSource) {
        this.messageSource = messageSource;
        id = 1;
        items = new HashMap<>();
    }

    void checkId(Integer id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("id", messageSource.get("item.ItemRepository.notFoundById") + ": " + id);
        }
    }

    @Override
    public Item create(Item item) {
        if (item.getName() == null) {
            throw new ValidationException("name", messageSource.get("item.ItemRepository.notNullName"));
        }
        if (item.getDescription() == null) {
            throw new ValidationException("description", messageSource.get("item.ItemRepository.notNullDescription"));
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("description", messageSource.get("item.ItemRepository.notNullAvailable"));
        }
        item.setId(getId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item readById(Integer id) {
        checkId(id);
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
        checkId(item.getId());
        Item updatedItem = items.get(item.getId());
        if (!updatedItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("user" + ownerId.toString(), "item" + item.getId().toString());
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
