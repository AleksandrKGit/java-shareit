package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

@Mapper
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    ItemDtoToClient toDto(Item item);

    @Mapping(target = "owner.id", source = "ownerId")
    Item toModel(ItemDtoFromClient itemDto);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto toBookingDto(Booking booking);
}
