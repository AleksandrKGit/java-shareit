package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {
    ItemRepository itemRepository;

    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;

    BookingRepository bookingRepository;

    CommentRepository commentRepository;

    private ItemDtoToClient mapToItemDto(Item item, boolean showBooking, boolean showComments) {
        ItemDtoToClient itemDto = ItemMapper.INSTANCE.toDto(item);
        if (showBooking) {
            LocalDateTime now = LocalDateTime.now();
            Optional<Booking> last =
                    bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(item.getId(), now);
            Optional<Booking> next =
                    bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(item.getId(), now);
            itemDto.setLastBooking(ItemMapper.INSTANCE.toBookingDto(last.orElse(null)));
            itemDto.setNextBooking(ItemMapper.INSTANCE.toBookingDto(next.orElse(null)));
        }
        if (showComments) {
            itemDto.setComments(commentRepository.findByItem_IdOrderByCreatedDesc(item.getId()).stream()
                    .map(CommentMapper.INSTANCE::toDto).collect(Collectors.toSet()));
        }
        return itemDto;
    }

    @Override
    public ItemDtoToClient create(Long ownerId, ItemDtoFromClient itemDtoFromClient) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) {
            throw new NotFoundException("ownerId", messageSource.get("item.ItemService.notFoundOwnerById") + ": "
                    + ownerId);
        }

        itemDtoFromClient.setOwnerId(ownerId);

        Item item = itemRepository.save(ItemMapper.INSTANCE.toModel(itemDtoFromClient));
        item.setOwner(owner.get());
        return mapToItemDto(item, false, false);
    }

    @Override
    public ItemDtoToClient readById(Long userId, Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": " + id);
        }
        return mapToItemDto(item.get(), Objects.equals(item.get().getOwner().getId(), userId), true);
    }

    @Override
    public Set<ItemDtoToClient> readByOwner(Long ownerId) {
        return itemRepository.findByOwner_IdOrderByIdAsc(ownerId).stream().map(item -> mapToItemDto(item,
                        true, true)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<ItemDtoToClient> readByQuery(Long userId, String query) {
        return query == null || query.isEmpty() ? Set.of() : itemRepository.findByQuery(query).stream()
                        .map(item -> mapToItemDto(item, Objects.equals(item.getOwner().getId(), userId),
                                false))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public ItemDtoToClient update(Long ownerId, Long id, ItemDtoFromClient itemDtoFromClient) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": " + id);
        }
        if (!Objects.equals(item.get().getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("user" + ownerId, "item" + id);
        }

        if (itemDtoFromClient.getName() != null) {
            item.get().setName(itemDtoFromClient.getName());
        }
        if (itemDtoFromClient.getDescription() != null) {
            item.get().setDescription(itemDtoFromClient.getDescription());
        }
        if (itemDtoFromClient.getAvailable() != null) {
            item.get().setAvailable(itemDtoFromClient.getAvailable());
        }

        return mapToItemDto(itemRepository.save(item.get()), true, false);
    }

    private void validateCreatingComment(Long authorId, Long itemId) {
        if (bookingRepository.getItemBookingsCountForBooker(itemId, authorId, BookingStatus.APPROVED) == 0) {
            throw new ValidationException("comment", messageSource.get("item.ItemService.notBooked") + ": itemId "
                    + itemId + ", authorId " + authorId);
        }
    }

    @Override
    public CommentDtoToClient createComment(Long authorId, Long itemId, CommentDtoFromClient commentDtoFromClient) {
        validateCreatingComment(authorId, itemId);

        commentDtoFromClient.setAuthorId(authorId);
        commentDtoFromClient.setItemId(itemId);

        Comment comment = CommentMapper.INSTANCE.toModel(commentDtoFromClient);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        comment.setAuthor(userRepository.findById(authorId).orElse(null));
        return CommentMapper.INSTANCE.toDto(comment);
    }
}