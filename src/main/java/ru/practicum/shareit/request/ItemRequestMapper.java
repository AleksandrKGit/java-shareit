package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ItemRequestMapper {
    ItemRequestMapper INSTANCE = Mappers.getMapper(ItemRequestMapper.class);

    @Mapping(target = "requestorId", source = "requestor.id")
    ItemRequestDto toDto(ItemRequest itemRequest);

    @Mapping(target = "requestor.id", source = "requestorId")
    ItemRequest toModel(ItemRequestDto itemRequestDto);
}