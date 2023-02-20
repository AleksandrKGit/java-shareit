package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.request.dto.ItemRequestDtoFromClient;

import java.util.Objects;

public class ItemRequestDtoFromClientMatcher implements ArgumentMatcher<ItemRequestDtoFromClient> {
    private final ItemRequestDtoFromClient dto;

    private ItemRequestDtoFromClientMatcher(ItemRequestDtoFromClient dto) {
        this.dto = dto;
    }

    public static ItemRequestDtoFromClientMatcher equalToDto(ItemRequestDtoFromClient dto) {
        return new ItemRequestDtoFromClientMatcher(dto);
    }

    @Override
    public boolean matches(ItemRequestDtoFromClient dto) {
        return dto != null && this.dto != null
                && Objects.equals(this.dto.getDescription(), dto.getDescription());
    }
}