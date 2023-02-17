package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDtoToClient toDto(Booking entity);

    @Mapping(target = "item.id", source = "itemId")
    Booking toEntity(BookingDtoFromClient dto);
}