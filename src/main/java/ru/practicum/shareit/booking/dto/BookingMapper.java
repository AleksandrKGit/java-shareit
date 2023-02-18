package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    List<BookingDtoToClient> toDtoList(List<Booking> entity);

    BookingDtoToClient toDto(Booking entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingDtoFromClient dto);
}