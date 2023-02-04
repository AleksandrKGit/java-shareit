package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;

    ItemRepository itemRepository;

    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;

    private void validateCreatingBooking(BookingDtoFromClient bookingDtoFromClient) {
        if (!bookingDtoFromClient.getStart().isBefore(bookingDtoFromClient.getEnd())) {
            throw new ValidationException("start", messageSource.get("booking.BookingService.startBeforeEnd"));
        }

        if (bookingRepository.getApprovedBookingsCountInPeriodForItem(bookingDtoFromClient.getItemId(),
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

        Optional<Item> item = itemRepository.findById(bookingDtoFromClient.getItemId());
        if (item.isEmpty()) {
            throw new NotFoundException("itemId", messageSource.get("booking.BookingService.notFoundItemById") + ": "
                    + bookingDtoFromClient.getItemId());
        }
        if (Objects.equals(item.get().getOwner().getId(), bookerId)) {
            throw new NotFoundException("itemId", item.get().getId() + " for user with id " + bookerId);
        }

        validateCreatingBookingItem(item.get());

        Optional<User> booker = userRepository.findById(bookerId);
        if (booker.isEmpty()) {
            throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundBookerById")
                    + ": " + bookerId);
        }

        bookingDtoFromClient.setStatus(BookingStatus.WAITING);
        bookingDtoFromClient.setBookerId(bookerId);

        Booking booking = bookingRepository.save(BookingMapper.INSTANCE.toModel(bookingDtoFromClient));
        booking.setItem(item.get());
        booking.setBooker(booker.get());
        return BookingMapper.INSTANCE.toDto(booking);
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

        if (approved && (bookingRepository.getApprovedBookingsCountInPeriodForItem(booking.getItem().getId(),
                BookingStatus.APPROVED, booking.getStart(), booking.getEnd()) > 0)) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemIsReserved") + ": "
                    + booking.getItem().getId() + " " + booking.getStart() + " " + booking.getEnd());
        }
    }

    @Override
    public BookingDtoToClient approve(Long id, Long ownerId, boolean approved) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("booking.BookingService.notFoundById") + ": " + id);
        }
        if (!Objects.equals(booking.get().getItem().getOwner().getId(), ownerId)) {
            throw new NotFoundException("itemId", booking.get().getItem().getId() + " for user with id " + ownerId);
        }

        validateApprovingBooking(booking.get(), approved);

        booking.get().setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.INSTANCE.toDto(bookingRepository.save(booking.get()));
    }

    @Override
    public BookingDtoToClient readById(Long id, Long userId) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("booking.BookingService.notFoundById") + ": " + id);
        }
        if (!Objects.equals(booking.get().getBooker().getId(), userId) &&
                !Objects.equals(booking.get().getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("itemId", booking.get().getItem().getId() + " for user with id " + userId);
        }
        return BookingMapper.INSTANCE.toDto(booking.get());
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
    public Set<BookingDtoToClient> readByBooker(Long bookerId, String state) {
        Set<Booking> bookings;
        switch (getValidBookingState(state)) {
            case CURRENT:
                bookings = bookingRepository.findCurrentForBooker(bookerId);
                break;
            case PAST:
                bookings = bookingRepository.findPastForBooker(bookerId);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureForBooker(bookerId);
                break;
            case WAITING:
                bookings = bookingRepository.findByStatusForBooker(bookerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByStatusForBooker(bookerId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findAllForBooker(bookerId);
        }

        if (bookings.size() == 0) {
            throw new NotFoundException("noBookingsFound", messageSource.get("booking.BookingService.noBookingsFound"));
        }

        return bookings.stream().map(BookingMapper.INSTANCE::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<BookingDtoToClient> readByOwner(Long ownerId, String state) {
        Set<Booking> bookings;
        switch (getValidBookingState(state)) {
            case CURRENT:
                bookings = bookingRepository.findCurrentForOwner(ownerId);
                break;
            case PAST:
                bookings = bookingRepository.findPastForOwner(ownerId);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureForOwner(ownerId);
                break;
            case WAITING:
                bookings = bookingRepository.findByStatusForOwner(ownerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByStatusForOwner(ownerId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findAllForOwner(ownerId);
        }

        if (bookings.size() == 0) {
            throw new NotFoundException("noBookingsFound", messageSource.get("booking.BookingService.noBookingsFound"));
        }

        return bookings.stream().map(BookingMapper.INSTANCE::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}