package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
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
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;

    ItemRepository itemRepository;

    UserRepository userRepository;

    DefaultLocaleMessageSource messageSource;


    @Override
    public BookingDtoToClient create(BookingDtoFromClient bookingDtoFromClient) {
        if (bookingDtoFromClient == null) {
            throw new ValidationException("booking", messageSource.get("booking.BookingService.notNullBooking"));
        }
        if (bookingDtoFromClient.getBookerId() == null) {
            throw new ValidationException("bookerId", messageSource.get("booking.BookingService.notNullBookerId"));
        }
        if (bookingDtoFromClient.getItemId() == null) {
            throw new ValidationException("itemId", messageSource.get("booking.BookingService.notNullItemId"));
        }
        if (bookingDtoFromClient.getStart() == null) {
            throw new ValidationException("start", messageSource.get("booking.BookingService.notNullStart"));
        }
        if (bookingDtoFromClient.getEnd() == null) {
            throw new ValidationException("end", messageSource.get("booking.BookingService.notNullEnd"));
        }
        if (!bookingDtoFromClient.getStart().isBefore(bookingDtoFromClient.getEnd())) {
            throw new ValidationException("start", messageSource.get("booking.BookingService.startBeforeEnd"));
        }

        if (bookingRepository.getApprovedBookingsCountInPeriodForItem(bookingDtoFromClient.getItemId(),
                BookingStatus.APPROVED, bookingDtoFromClient.getStart(), bookingDtoFromClient.getEnd()) > 0) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemIsReserved") + ": "
                    + bookingDtoFromClient.getItemId() + " " + bookingDtoFromClient.getStart() + " "
                    + bookingDtoFromClient.getEnd());
        }

        bookingDtoFromClient.setStatus(BookingStatus.WAITING);

        Optional<Item> item = itemRepository.findById(bookingDtoFromClient.getItemId());
        if (item.isEmpty()) {
            throw new NotFoundException("itemId", messageSource.get("booking.BookingService.notFoundItemById") + ": "
                    + bookingDtoFromClient.getItemId());
        }
        if (!item.get().getAvailable()) {
            throw new ValidationException("itemId", messageSource.get("booking.BookingService.itemNotAvailable"));
        }
        if (Objects.equals(item.get().getOwner().getId(), bookingDtoFromClient.getBookerId())) {
            throw new NotFoundException("itemId", item.get().getId() + " for user with id "
                    + bookingDtoFromClient.getBookerId());
        }

        /*
         * КОСТЫЛЬ ДЛЯ ТЕСТОВ POSTMAN
         * Здесь идёт дополнительный запрос к базе данных ввиду того, что при ошибке операции INSERT, которая возникает
         * из-за несуществующего booker_id, PostgreSQL инкрементирует значение id, а тесты Postman это не учитывают.
         */
        if (!userRepository.existsById(bookingDtoFromClient.getBookerId())) {
            throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundBookerById")
                    + ": " + bookingDtoFromClient.getBookerId());
        }

        try {
            Booking booking = bookingRepository.save(BookingMapper.toBooking(bookingDtoFromClient));
            booking.setItem(item.get());
            return BookingMapper.toBookingDto(booking);
        } catch (DataIntegrityViolationException exception) {
            // Обработка несуществующего booker_id без дополнительного запроса к БД
            if (ConstraintChecker.check(exception, "fk_booking_booker")) {
                throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundBookerById")
                        + ": " + bookingDtoFromClient.getBookerId());
            } else {
                throw exception;
            }
        }

    }

    @Override
    public BookingDtoToClient approve(Long id, Long ownerId, boolean approved) {
        if (id == null) {
            throw new ValidationException("id", messageSource.get("booking.BookingService.notNullId"));
        }
        if (ownerId == null) {
            throw new ValidationException("ownerId", messageSource.get("booking.BookingService.notNullOwnerId"));
        }

        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            throw new NotFoundException("id", messageSource.get("booking.BookingService.notFoundById") + ": " + id);
        }
        if (!Objects.equals(booking.get().getItem().getOwner().getId(), ownerId)) {
            throw new NotFoundException("itemId", booking.get().getItem().getId() + " for user with id " + ownerId);
        }
        if (!booking.get().getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("status", messageSource.get("booking.BookingService.statusIsWaiting")
                    + ": " + booking.get().getStatus());
        }
        if (!booking.get().getItem().getAvailable()) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemNotAvailable")
                    + ": " + booking.get().getItem().getId());
        }
        if (!booking.get().getEnd().isAfter(LocalDateTime.now())) {
            throw new ValidationException("id", messageSource.get("booking.BookingService.endInFuture"));
        }

        if (approved && (bookingRepository.getApprovedBookingsCountInPeriodForItem(booking.get().getItem().getId(),
                BookingStatus.APPROVED, booking.get().getStart(), booking.get().getEnd()) > 0)) {
            throw new ValidationException("item", messageSource.get("booking.BookingService.itemIsReserved") + ": "
                    + booking.get().getItem().getId() + " " + booking.get().getStart() + " " + booking.get().getEnd());
        }

        booking.get().setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingDto(bookingRepository.save(booking.get()));
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
        return BookingMapper.toBookingDto(booking.get());
    }

    @Override
    public Set<BookingDtoToClient> readByBooker(Long bookerId, BookingState state) {
        if (bookerId == null) {
            throw new ValidationException("bookerId", messageSource.get("booking.BookingService.notNullBookerId"));
        }
        /*
         * КОСТЫЛЬ ДЛЯ ТЕСТОВ POSTMAN
         * В случае, если пользователь не найден, Postman ожидает ошибку Not Found, а не пустой список в ответ
         */
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundBookerById")
                    + ": " + bookerId);
        }

        if (state == null) {
            state = BookingState.ALL;
        }
        Set<Booking> bookings;
        switch (state) {
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
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<BookingDtoToClient> readByOwner(Long ownerId, BookingState state) {
        if (ownerId == null) {
            throw new ValidationException("bookerId", messageSource.get("booking.BookingService.notNullOwnerId"));
        }
        /*
         * КОСТЫЛЬ ДЛЯ ТЕСТОВ POSTMAN
         * В случае, если пользователь не найден, Postman ожидает ошибку Not Found, а не пустой список в ответ
         */
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("bookerId", messageSource.get("booking.BookingService.notFoundOwnerById")
                    + ": " + ownerId);
        }

        if (state == null) {
            state = BookingState.ALL;
        }
        Set<Booking> bookings;
        switch (state) {
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
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}