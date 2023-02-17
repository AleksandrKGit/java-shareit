package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;

import java.util.Objects;

public class ItemDtoFromClientMatcher implements ArgumentMatcher<ItemDtoFromClient> {
    private final ItemDtoFromClient dto;

    private ItemDtoFromClientMatcher(ItemDtoFromClient dto) {
        this.dto = dto;
    }

    public static ItemDtoFromClientMatcher equalToDto(ItemDtoFromClient dto) {
        return new ItemDtoFromClientMatcher(dto);
    }

    @Override
    public boolean matches(ItemDtoFromClient dto) {
        return dto != null && this.dto != null
                && Objects.equals(this.dto.getName(), dto.getName())
                && Objects.equals(this.dto.getRequestId(), dto.getRequestId())
                && Objects.equals(this.dto.getDescription(), dto.getDescription())
                && Objects.equals(this.dto.getAvailable(), dto.getAvailable());
    }
}
