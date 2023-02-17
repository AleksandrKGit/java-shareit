package ru.practicum.shareit.item.dto;

import org.mapstruct.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "requestId", source = "request.id")
    ItemDtoToClient toDto(Item entity);

    Item toEntity(ItemDtoFromClient dto);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingDtoToClient toDto(Booking entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ItemDtoFromClient dto, @MappingTarget Item entity);
}
