package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.booking.model.Booking;

@Mapper
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "item.id", source = "item.id")
    @Mapping(target = "item.name", source = "item.name")
    @Mapping(target = "booker.id", source = "booker.id")
    BookingDtoToClient toDto(Booking booking);

    @Mapping(target = "item.id", source = "itemId")
    @Mapping(target = "booker.id", source = "bookerId")
    Booking toModel(BookingDtoFromClient bookingDto);
}