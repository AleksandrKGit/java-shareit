package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
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
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
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
        ItemDtoToClient itemDto = ItemMapper.toItemDto(item);
        if (showBooking) {
            LocalDateTime now = LocalDateTime.now();
            Optional<Booking> last =
                    bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(item.getId(), now);
            Optional<Booking> next =
                    bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(item.getId(), now);
            itemDto.setLastBooking(ItemMapper.toBookingDto(last.orElse(null)));
            itemDto.setNextBooking(ItemMapper.toBookingDto(next.orElse(null)));
        }
        if (showComments) {
            itemDto.setComments(commentRepository.findByItem_IdOrderByCreatedDesc(item.getId()).stream()
                    .map(CommentMapper::toCommentDto).collect(Collectors.toSet()));
        }
        return itemDto;
    }

    @Override
    public ItemDtoToClient create(ItemDtoFromClient itemDtoFromClient) {
        if (itemDtoFromClient == null) {
            throw new ValidationException("item", messageSource.get("item.ItemService.notNullItem"));
        }
        if (itemDtoFromClient.getOwnerId() == null) {
            throw new ValidationException("ownerId", messageSource.get("item.ItemService.notNullOwnerId"));
        }
        if (itemDtoFromClient.getName() == null) {
            throw new ValidationException("name", messageSource.get("item.ItemService.notNullName"));
        }
        if (itemDtoFromClient.getDescription() == null) {
            throw new ValidationException("description", messageSource.get("item.ItemService.notNullDescription"));
        }
        if (itemDtoFromClient.getAvailable() == null) {
            throw new ValidationException("available", messageSource.get("item.ItemService.notNullAvailable"));
        }

        /*
         * КОСТЫЛЬ ДЛЯ ТЕСТОВ POSTMAN
         * Здесь идёт дополнительный запрос к базе данных ввиду того, что при ошибке операции INSERT, которая возникает
         * из-за несуществующего owner_id, PostgreSQL инкрементирует значение id, а тесты Postman это не учитывают.
         */
        if (!userRepository.existsById(itemDtoFromClient.getOwnerId())) {
            throw new NotFoundException("ownerId", messageSource.get("item.ItemService.notFoundOwnerById") + ": "
                    + itemDtoFromClient.getOwnerId());
        }

        try {
            return mapToItemDto(itemRepository.save(ItemMapper.toItem(itemDtoFromClient)),
                    false, false);
        } catch (DataIntegrityViolationException exception) {
            // Обработка несуществующего owner_id без дополнительного запроса к БД
            if (ConstraintChecker.check(exception, "fk_item_owner")) {
                throw new NotFoundException("ownerId", messageSource.get("item.ItemService.notFoundOwnerById") + ": "
                        + itemDtoFromClient.getOwnerId());
            } else if (ConstraintChecker.check(exception, "fk_item_request")) {
                throw new NotFoundException("request_id", messageSource.get("item.ItemService.notFoundRequestById")
                        + ": " + itemDtoFromClient.getRequestId());
            } else {
                throw exception;
            }
        }
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
    public ItemDtoToClient update(ItemDtoFromClient itemDtoFromClient) {
        if (itemDtoFromClient == null) {
            throw new ValidationException("item", messageSource.get("item.ItemService.notNullItem"));
        }
        if (itemDtoFromClient.getOwnerId() == null) {
            throw new ValidationException("owner", messageSource.get("item.ItemService.notNullOwnerId"));
        }
        if (itemDtoFromClient.getId() == null) {
            throw new ValidationException("id", messageSource.get("item.ItemService.notNullId"));
        }

        Optional<Item> item = itemRepository.findById(itemDtoFromClient.getId());
        if (item.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": "
                    + itemDtoFromClient.getId());
        }
        if (!Objects.equals(item.get().getOwner().getId(), itemDtoFromClient.getOwnerId())) {
            throw new AccessDeniedException("user" + itemDtoFromClient.getOwnerId(), "item"
                    + itemDtoFromClient.getId());
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

    @Override
    public CommentDtoToClient createComment(CommentDtoFromClient commentDtoFromClient) {
        if (commentDtoFromClient == null) {
            throw new ValidationException("comment", messageSource.get("item.ItemService.notNullComment"));
        }
        if (commentDtoFromClient.getAuthorId() == null) {
            throw new ValidationException("authorId", messageSource.get("item.ItemService.notNullAuthorId"));
        }
        if (commentDtoFromClient.getItemId() == null) {
            throw new ValidationException("itemId", messageSource.get("item.ItemService.notNullId"));
        }
        if (bookingRepository.getItemBookingsCountForBooker(commentDtoFromClient.getItemId(),
                commentDtoFromClient.getAuthorId(), BookingStatus.APPROVED) == 0) {
            throw new ValidationException("comment", messageSource.get("item.ItemService.notBooked") + ": itemId "
                    + commentDtoFromClient.getItemId() + ", authorId " + commentDtoFromClient.getAuthorId());
        }

        Comment comment = CommentMapper.toComment(commentDtoFromClient);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        comment.setAuthor(userRepository.findById(comment.getAuthor().getId()).orElse(null));
        return CommentMapper.toCommentDto(comment);
    }
}