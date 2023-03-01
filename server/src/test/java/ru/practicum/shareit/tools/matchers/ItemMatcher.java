package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.item.model.Item;
import java.util.Objects;

public class ItemMatcher implements ArgumentMatcher<Item> {
    private final Item entity;

    private ItemMatcher(Item entity) {
        this.entity = entity;
    }

    public static ItemMatcher equalToItem(Item entity) {
        return new ItemMatcher(entity);
    }

    @Override
    public boolean matches(Item entity) {
        return entity != null && this.entity != null
                && Objects.equals(this.entity.getId(), entity.getId())
                && Objects.equals(this.entity.getName(), entity.getName())
                && Objects.equals(this.entity.getDescription(), entity.getDescription())
                && Objects.equals(this.entity.getAvailable(), entity.getAvailable())
                && Objects.equals(this.entity.getRequest(), entity.getRequest())
                && Objects.equals(this.entity.getOwner(), entity.getOwner());
    }
}