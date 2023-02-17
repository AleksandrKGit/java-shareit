package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.OffsetPageRequest;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ItemServiceImpl implements ItemService {
    ItemRepository repository;

    ItemRequestRepository itemRequestRepository;

    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;

    BookingRepository bookingRepository;

    CommentRepository commentRepository;

    ItemMapper mapper;

    CommentMapper commentMapper;

    private ItemDtoToClient mapToItemDto(Item item, boolean showBooking, boolean showComments) {
        ItemDtoToClient dto = mapper.toDto(item);

        if (showBooking) {
            LocalDateTime now = LocalDateTime.now();

            Booking last = bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(item.getId(), now)
                    .orElse(null);
            Booking next = bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(item.getId(), now)
                    .orElse(null);

            dto.setLastBooking(mapper.toDto(last));
            dto.setNextBooking(mapper.toDto(next));
        }

        if (showComments) {
            dto.setComments(commentRepository.findByItem_IdOrderByCreatedDesc(item.getId()).stream()
                    .map(commentMapper::toDto).collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public ItemDtoToClient create(Long ownerId, ItemDtoFromClient dto) {
        User owner = userRepository.findById(ownerId).orElse(null);

        if (owner == null) {
            throw new NotFoundException("ownerId", messageSource.get("item.ItemService.notFoundOwnerById") + ": "
                    + ownerId);
        }

        Item entity = mapper.toEntity(dto);
        entity.setOwner(owner);

        if (dto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(dto.getRequestId()).orElse(null);

            if (itemRequest == null) {
                throw new NotFoundException("requestId", messageSource.get("item.ItemService.notFoundRequestById") + ": "
                        + dto.getRequestId());
            }

            entity.setRequest(itemRequest);
        }

        entity = repository.saveAndFlush(entity);

        return mapToItemDto(entity, false, false);
    }

    private void validateItemWasBooked(Long authorId, Long itemId) {
        if (bookingRepository.getItemBookingsCountForBooker(itemId, authorId, BookingStatus.APPROVED) == 0) {
            throw new ValidationException("comment", messageSource.get("item.ItemService.notBooked") + ": itemId "
                    + itemId + ", authorId " + authorId);
        }
    }

    @Override
    public CommentDtoToClient createComment(Long authorId, Long itemId, CommentDtoFromClient commentDtoFromClient) {
        validateItemWasBooked(authorId, itemId);

        User author = userRepository.findById(authorId).orElse(null);

        if (author == null) {
            throw new NotFoundException("authorId", messageSource.get("item.ItemService.notFoundAuthorById") + ": "
                    + authorId);
        }

        Item item = new Item();
        item.setId(itemId);

        Comment entity = commentMapper.toEntity(commentDtoFromClient);
        entity.setAuthor(author);
        entity.setItem(item);
        entity.setCreated(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.saveAndFlush(entity));
    }

    @Override
    public List<ItemDtoToClient> readByOwner(Long ownerId, Integer from, Integer size) {
        return repository.findByOwner_Id(ownerId,
                        OffsetPageRequest.ofOffset(from, size, Sort.by("id").ascending()))
                .stream()
                .map(item -> mapToItemDto(item, true, true))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDtoToClient> readByQuery(Long userId, String query, Integer from, Integer size) {
        return query == null || query.isEmpty() ? List.of() : repository.findByQuery(query,
                        OffsetPageRequest.ofOffset(from, size, Sort.by("id").ascending()))
                .stream()
                .map(item -> mapToItemDto(item, Objects.equals(item.getOwner().getId(), userId),
                        false))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoToClient readById(Long userId, Long id) {
        Item entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": " + id);
        }

        return mapToItemDto(entity, Objects.equals(entity.getOwner().getId(), userId), true);
    }

    @Override
    public ItemDtoToClient update(Long ownerId, Long id, ItemDtoFromClient dto) {
        Item entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("item.ItemService.notFoundById") + ": " + id);
        }

        if (!Objects.equals(entity.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("user" + ownerId, "item" + id);
        }

        mapper.updateEntityFromDto(dto, entity);

        return mapToItemDto(repository.saveAndFlush(entity), true, false);
    }
}