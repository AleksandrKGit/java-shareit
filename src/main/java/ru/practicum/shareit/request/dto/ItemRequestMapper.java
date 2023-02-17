package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {
    ItemRequestDtoToClient toDto(ItemRequest itemRequest);

    @Mapping(target = "requestId", source = "request.id")
    ItemDtoToClient toDto(Item item);

    ItemRequest toEntity(ItemRequestDtoFromClient itemRequestDtoFromClient);
}