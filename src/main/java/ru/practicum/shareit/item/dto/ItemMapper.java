package ru.practicum.shareit.item.dto;

import org.mapstruct.*;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "requestId", source = "entity.request.id")
    ItemDtoToClient toDto(Item entity, @Context Long userId, @Context BookingRepository bookingRepository,
                          @Context CommentRepository commentRepository);

    List<ItemDtoToClient> toDtoList(List<Item> entities, @Context Long userId,
                                    @Context BookingRepository bookingRepository,
                                    @Context CommentRepository commentRepository);

    @AfterMapping
    default void toDto(@MappingTarget ItemDtoToClient target, Item entity, @Context Long userId,
                       @Context BookingRepository bookingRepository, @Context CommentRepository commentRepository) {
        if (entity == null || entity.getId() == null) {
            return;
        }

        if (bookingRepository != null && userId != null && entity.getOwner() != null
                && userId.equals(entity.getOwner().getId())) {
            LocalDateTime now = LocalDateTime.now();

            Booking last = bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(entity.getId(), now)
                    .orElse(null);
            Booking next = bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(entity.getId(), now)
                    .orElse(null);

            target.setLastBooking(toBookingDto(last));
            target.setNextBooking(toBookingDto(next));
        }

        if (commentRepository != null) {
            target.setComments(toCommentDtoList(commentRepository.findByItem_IdOrderByCreatedDesc(entity.getId())));
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    Item toEntity(ItemDtoFromClient dto);

    List<CommentDtoToClient> toCommentDtoList(List<Comment> entities);

    @Mapping(target = "authorName", source = "author.name")
    CommentDtoToClient toCommentDto(Comment entity);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingDtoToClient toBookingDto(Booking entity);

    @Mapping(target = "request", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ItemDtoFromClient dto, @MappingTarget Item entity);
}