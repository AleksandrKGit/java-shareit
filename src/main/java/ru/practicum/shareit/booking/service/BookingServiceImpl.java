package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.OffsetPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.support.DefaultLocaleMessageSource;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    BookingRepository repository;

    ItemRepository itemRepository;

    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;

    BookingMapper mapper;

    private void validateCreatingBooking(BookingDtoFromClient bookingDtoFromClient) {
        if (!bookingDtoFromClient.getStart().isBefore(bookingDtoFromClient.getEnd())) {
            throw new ValidationException("start", messageSource.get("booking.BookingService.startBeforeEnd") + ": "
                    + bookingDtoFromClient.getStart() + " " + bookingDtoFromClient.getEnd());
        }

        if (repository.getApprovedBookingsCountInPeriodForItem(bookingDtoFromClient.getItemId(),
                BookingStatus.APPROVED, bookingDtoFromClient.getStart(), bookingDtoFromClient.getEnd()) > 0) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemIsReserved") + ": "
                    + bookingDtoFromClient.getItemId() + " " + bookingDtoFromClient.getStart() + " "
                    + bookingDtoFromClient.getEnd());
        }
    }

    private void validateCreatingBookingItem(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("itemId", messageSource.get("booking.BookingService.itemNotAvailable"));
        }
    }

    @Override
    public BookingDtoToClient create(Long bookerId, BookingDtoFromClient bookingDtoFromClient) {
        validateCreatingBooking(bookingDtoFromClient);

        Item item = itemRepository.findById(bookingDtoFromClient.getItemId()).orElse(null);

        if (item == null) {
            throw new NotFoundException("itemId", messageSource.get("booking.BookingService.notFoundItemById") + ": "
                    + bookingDtoFromClient.getItemId());
        }

        if (Objects.equals(item.getOwner().getId(), bookerId)) {
            throw new NotFoundException("itemId", item.getId() + " for user with id " + bookerId);
        }

        validateCreatingBookingItem(item);

        User booker = userRepository.findById(bookerId).orElse(null);

        if (booker == null) {
            throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundBookerById")
                    + ": " + bookerId);
        }

        Booking entity = mapper.toEntity(bookingDtoFromClient);
        entity.setItem(item);
        entity.setBooker(booker);
        entity.setStatus(BookingStatus.WAITING);

        return mapper.toDto(repository.saveAndFlush(entity));
    }

    private BookingState getValidBookingState(String state) {
        BookingState enumState = BookingState.ALL;

        if (state != null) {
            try {
                enumState = BookingState.valueOf(state);
            } catch (IllegalArgumentException ignored) {
                throw new ValidationException("error", "Unknown state: UNSUPPORTED_STATUS");
            }
        }

        return enumState;
    }

    @Override
    public List<BookingDtoToClient> readByBooker(Long bookerId, String state, Integer from, Integer size) {
        Page<Booking> entities;
        Pageable pageable = OffsetPageRequest.ofOffset(from, size, Sort.by("start").descending());

        switch (getValidBookingState(state)) {
            case CURRENT:
                entities = repository.findCurrentForBooker(bookerId, pageable);
                break;
            case PAST:
                entities = repository.findPastForBooker(bookerId, pageable);
                break;
            case FUTURE:
                entities = repository.findFutureForBooker(bookerId, pageable);
                break;
            case WAITING:
                entities = repository.findByStatusForBooker(bookerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                entities = repository.findByStatusForBooker(bookerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                entities = repository.findAllForBooker(bookerId, pageable);
        }

        if (entities.getContent().size() == 0) {
            throw new NotFoundException("noBookingsFound", messageSource.get("booking.BookingService.noBookingsFound"));
        }

        return entities.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoToClient> readByOwner(Long ownerId, String state, Integer from, Integer size) {
        Page<Booking> entities;
        Pageable pageable = OffsetPageRequest.ofOffset(from, size, Sort.by("start").descending());

        switch (getValidBookingState(state)) {
            case CURRENT:
                entities = repository.findCurrentForOwner(ownerId, pageable);
                break;
            case PAST:
                entities = repository.findPastForOwner(ownerId, pageable);
                break;
            case FUTURE:
                entities = repository.findFutureForOwner(ownerId, pageable);
                break;
            case WAITING:
                entities = repository.findByStatusForOwner(ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                entities = repository.findByStatusForOwner(ownerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                entities = repository.findAllForOwner(ownerId, pageable);
        }

        if (entities.getContent().size() == 0) {
            throw new NotFoundException("noBookingsFound", messageSource.get("booking.BookingService.noBookingsFound"));
        }

        return entities.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public BookingDtoToClient readById(Long id, Long userId) {
        Booking entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("booking.BookingService.notFoundById") + ": " + id);
        }

        if (!Objects.equals(entity.getBooker().getId(), userId) &&
                !Objects.equals(entity.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("itemId", entity.getItem().getId() + " for user with id " + userId);
        }

        return mapper.toDto(entity);
    }

    private void validateApprovingBooking(Booking booking, boolean approved) {
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("status", messageSource.get("booking.BookingService.statusIsWaiting")
                    + ": " + booking.getStatus());
        }

        if (!booking.getItem().getAvailable()) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemNotAvailable")
                    + ": " + booking.getItem().getId());
        }

        if (!booking.getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("id", messageSource.get("booking.BookingService.endInFuture"));
        }

        if (approved && (repository.getApprovedBookingsCountInPeriodForItem(booking.getItem().getId(),
                BookingStatus.APPROVED, booking.getStart(), booking.getEnd()) > 0)) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemIsReserved") + ": "
                    + booking.getItem().getId() + " " + booking.getStart() + " " + booking.getEnd());
        }
    }

    @Override
    public BookingDtoToClient approve(Long id, Long ownerId, boolean approved) {
        Booking entity = repository.findById(id).orElse(null);

        if (entity == null) {
            throw new NotFoundException("id", messageSource.get("booking.BookingService.notFoundById") + ": " + id);
        }

        if (!Objects.equals(entity.getItem().getOwner().getId(), ownerId)) {
            throw new NotFoundException("itemId", entity.getItem().getId() + " for user with id " + ownerId);
        }

        validateApprovingBooking(entity, approved);

        entity.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return mapper.toDto(repository.saveAndFlush(entity));
    }
}