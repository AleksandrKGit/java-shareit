package ru.practicum.shareit.booking.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.dto.BookingMapperImpl;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.tools.factories.BookingFactory.*;
import static ru.practicum.shareit.tools.factories.UserFactory.*;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.matchers.BookingMatcher.equalToBooking;

@SpringBootTest(classes = {BookingServiceImpl.class, BookingMapperImpl.class})
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceTest {
    @Autowired
    BookingService service;

    @MockBean
    BookingRepository repository;

    @MockBean
    ItemRepository itemRepository;

    @MockBean
    UserRepository userRepository;

    BookingDtoFromClient requestBookingDto;

    final Long id = 1L;

    Booking createdBooking;

    Booking existingBooking;

    static final Long userId = 2L;

    User user;

    static final Long ownerId = 3L;

    static final Long otherUserId = 4L;

    Item item;

    final int from = 5;

    final int size = 15;

    OffsetPageRequest offsetPageRequest;

    Page<Booking> emptyList;

    Page<Booking> bookings;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        Long itemId = 5L;
        requestBookingDto = createBookingDtoFromClient(itemId, now.plusDays(1), now.plusDays(2));
        user = createUser(userId, null, null);
        item = createItem(itemId, "itemName", null, true,
                createUser(ownerId, null, null), null);
        createdBooking = createBooking(id, requestBookingDto.getStart(), requestBookingDto.getEnd(),
                BookingStatus.WAITING, item, user);
        existingBooking = createBooking(id, createdBooking.getStart(), createdBooking.getEnd(), BookingStatus.WAITING,
                item, user);
        offsetPageRequest = OffsetPageRequest.ofOffset(from, size, Sort.by("start").descending());
        emptyList = new PageImpl<>(List.of());
        bookings = new PageImpl<>(List.of(copyOf(existingBooking)));
    }

    @Test
    void create_withPeriodWithReservedBookings_shouldThrowBadRequestException() {
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(1L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.of(copyOf(item)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(copyOf(user)));

        assertThrows(BadRequestException.class, () -> service.create(userId, requestBookingDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withWithNotExistingItem_shouldThrowNotFoundException() {
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(0L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(copyOf(user)));

        assertThrows(NotFoundException.class, () -> service.create(userId, requestBookingDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withUserItem_shouldThrowNotFoundException() {
        item.setOwner(user);
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(0L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.of(copyOf(item)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(copyOf(user)));

        assertThrows(NotFoundException.class, () -> service.create(userId, requestBookingDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withNotAvailableItem_shouldThrowBadRequestException() {
        item.setAvailable(false);
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(0L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.of(copyOf(item)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(copyOf(user)));

        assertThrows(BadRequestException.class, () -> service.create(userId, requestBookingDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withNotExistingUser_shouldThrowNotFoundException() {
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(0L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.of(copyOf(item)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(userId, requestBookingDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_shouldReturnDtoOfCreatedBooking() {
        ArgumentCaptor<Booking> bookingArgumentCaptor = ArgumentCaptor.forClass(Booking.class);
        when(repository.getApprovedBookingsCountInPeriodForItem(requestBookingDto.getItemId(),
                BookingStatus.APPROVED, requestBookingDto.getStart(), requestBookingDto.getEnd())).thenReturn(0L);
        when(itemRepository.findById(requestBookingDto.getItemId())).thenReturn(Optional.of(copyOf(item)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(copyOf(user)));
        when(repository.saveAndFlush(bookingArgumentCaptor.capture())).thenReturn(copyOf(createdBooking));

        BookingDtoToClient resultBookingDto = service.create(userId, requestBookingDto);
        Booking bookingToRepository = bookingArgumentCaptor.getValue();

        assertThat(bookingToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("status", equalTo(BookingStatus.WAITING)),
                hasProperty("start", equalTo(requestBookingDto.getStart())),
                hasProperty("end", equalTo(requestBookingDto.getEnd())),
                hasProperty("item", hasProperty("id", equalTo(requestBookingDto.getItemId()))),
                hasProperty("booker", hasProperty("id", equalTo(user.getId())))
        ));

        assertThat(resultBookingDto, allOf(
                hasProperty("id", equalTo(createdBooking.getId())),
                hasProperty("start", equalTo(createdBooking.getStart())),
                hasProperty("end", equalTo(createdBooking.getEnd())),
                hasProperty("status", equalTo(createdBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(createdBooking.getItem().getId())),
                        hasProperty("name", equalTo(createdBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(createdBooking.getBooker().getId())))
        ));
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void readByOwner_withEmptyBookingsList_shouldThrowNotFoundException(BookingState state) {
        switch (state) {
            case CURRENT:
                when(repository.findCurrentForOwner(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case PAST:
                when(repository.findPastForOwner(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case FUTURE:
                when(repository.findFutureForOwner(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case WAITING:
                when(repository.findByStatusForOwner(userId, BookingStatus.WAITING, offsetPageRequest))
                        .thenReturn(emptyList);
                break;
            case REJECTED:
                when(repository.findByStatusForOwner(userId, BookingStatus.REJECTED, offsetPageRequest))
                        .thenReturn(emptyList);
                break;
            default:
                when(repository.findAllForOwner(userId, offsetPageRequest)).thenReturn(emptyList);
        }

        assertThrows(NotFoundException.class, () -> service.readByOwner(userId, state, from, size));

        verify(repository, state == BookingState.CURRENT ? times(1) : never())
                .findCurrentForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.PAST ? times(1) : never())
                .findPastForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.FUTURE ? times(1) : never())
                .findFutureForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.WAITING ? times(1) : never())
                .findByStatusForOwner(userId, BookingStatus.WAITING, offsetPageRequest);
        verify(repository, state == BookingState.REJECTED ? times(1) : never())
                .findByStatusForOwner(userId, BookingStatus.REJECTED, offsetPageRequest);
        verify(repository, state == BookingState.ALL ? times(1) : never())
                .findAllForOwner(userId, offsetPageRequest);
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void readByOwner_shouldReturnDtoListOfSelectedOwnerBookings(BookingState state) {
        switch (state) {
            case CURRENT:
                when(repository.findCurrentForOwner(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case PAST:
                when(repository.findPastForOwner(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case FUTURE:
                when(repository.findFutureForOwner(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case WAITING:
                when(repository.findByStatusForOwner(userId, BookingStatus.WAITING, offsetPageRequest))
                        .thenReturn(bookings);
                break;
            case REJECTED:
                when(repository.findByStatusForOwner(userId, BookingStatus.REJECTED, offsetPageRequest))
                        .thenReturn(bookings);
                break;
            default:
                when(repository.findAllForOwner(userId, offsetPageRequest)).thenReturn(bookings);
        }

        List<BookingDtoToClient> resultBookingListDto = service.readByOwner(userId, state, from, size);

        assertThat(resultBookingListDto, contains(allOf(
                hasProperty("id", equalTo(existingBooking.getId())),
                hasProperty("start", equalTo(existingBooking.getStart())),
                hasProperty("end", equalTo(existingBooking.getEnd())),
                hasProperty("status", equalTo(existingBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(existingBooking.getItem().getId())),
                        hasProperty("name", equalTo(existingBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(existingBooking.getBooker().getId())))
        )));

        verify(repository, state == BookingState.CURRENT ? times(1) : never())
                .findCurrentForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.PAST ? times(1) : never())
                .findPastForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.FUTURE ? times(1) : never())
                .findFutureForOwner(userId, offsetPageRequest);
        verify(repository, state == BookingState.WAITING ? times(1) : never())
                .findByStatusForOwner(userId, BookingStatus.WAITING, offsetPageRequest);
        verify(repository, state == BookingState.REJECTED ? times(1) : never())
                .findByStatusForOwner(userId, BookingStatus.REJECTED, offsetPageRequest);
        verify(repository, state == BookingState.ALL ? times(1) : never())
                .findAllForOwner(userId, offsetPageRequest);
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void readByBooker_withEmptyBookingsList_shouldThrowNotFoundException(BookingState state) {
        switch (state) {
            case CURRENT:
                when(repository.findCurrentForBooker(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case PAST:
                when(repository.findPastForBooker(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case FUTURE:
                when(repository.findFutureForBooker(userId, offsetPageRequest)).thenReturn(emptyList);
                break;
            case WAITING:
                when(repository.findByStatusForBooker(userId, BookingStatus.WAITING, offsetPageRequest))
                        .thenReturn(emptyList);
                break;
            case REJECTED:
                when(repository.findByStatusForBooker(userId, BookingStatus.REJECTED, offsetPageRequest))
                        .thenReturn(emptyList);
                break;
            default:
                when(repository.findAllForBooker(userId, offsetPageRequest)).thenReturn(emptyList);
        }

        assertThrows(NotFoundException.class, () -> service.readByBooker(userId, state, from, size));

        verify(repository, state == BookingState.CURRENT ? times(1) : never())
                .findCurrentForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.PAST ? times(1) : never())
                .findPastForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.FUTURE ? times(1) : never())
                .findFutureForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.WAITING ? times(1) : never())
                .findByStatusForBooker(userId, BookingStatus.WAITING, offsetPageRequest);
        verify(repository, state == BookingState.REJECTED ? times(1) : never())
                .findByStatusForBooker(userId, BookingStatus.REJECTED, offsetPageRequest);
        verify(repository, state == BookingState.ALL ? times(1) : never())
                .findAllForBooker(userId, offsetPageRequest);
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void readByBooker_shouldReturnDtoListOfSelectedBookerBookings(BookingState state) {
        switch (state) {
            case CURRENT:
                when(repository.findCurrentForBooker(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case PAST:
                when(repository.findPastForBooker(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case FUTURE:
                when(repository.findFutureForBooker(userId, offsetPageRequest)).thenReturn(bookings);
                break;
            case WAITING:
                when(repository.findByStatusForBooker(userId, BookingStatus.WAITING, offsetPageRequest))
                        .thenReturn(bookings);
                break;
            case REJECTED:
                when(repository.findByStatusForBooker(userId, BookingStatus.REJECTED, offsetPageRequest))
                        .thenReturn(bookings);
                break;
            default:
                when(repository.findAllForBooker(userId, offsetPageRequest)).thenReturn(bookings);
        }

        List<BookingDtoToClient> resultBookingListDto = service.readByBooker(userId, state, from, size);

        assertThat(resultBookingListDto, contains(allOf(
                hasProperty("id", equalTo(existingBooking.getId())),
                hasProperty("start", equalTo(existingBooking.getStart())),
                hasProperty("end", equalTo(existingBooking.getEnd())),
                hasProperty("status", equalTo(existingBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(existingBooking.getItem().getId())),
                        hasProperty("name", equalTo(existingBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(existingBooking.getBooker().getId())))
        )));

        verify(repository, state == BookingState.CURRENT ? times(1) : never())
                .findCurrentForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.PAST ? times(1) : never())
                .findPastForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.FUTURE ? times(1) : never())
                .findFutureForBooker(userId, offsetPageRequest);
        verify(repository, state == BookingState.WAITING ? times(1) : never())
                .findByStatusForBooker(userId, BookingStatus.WAITING, offsetPageRequest);
        verify(repository, state == BookingState.REJECTED ? times(1) : never())
                .findByStatusForBooker(userId, BookingStatus.REJECTED, offsetPageRequest);
        verify(repository, state == BookingState.ALL ? times(1) : never())
                .findAllForBooker(userId, offsetPageRequest);
    }

    static Stream<Arguments> userIds() {
        return Stream.of(
                Arguments.of("bookerId", userId),
                Arguments.of("ownerId", ownerId),
                Arguments.of("otherUserId", otherUserId)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("userIds")
    void readById_withNotExistingId_shouldThrowNotFoundException(String testName, Long userId) {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readById(id, userId));
    }

    @Test
    void readById_withNotBookerOrItemOwner_shouldThrowNotFoundException() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(NotFoundException.class, () -> service.readById(id, otherUserId));
    }

    static Stream<Arguments> allowedUserIds() {
        return Stream.of(
                Arguments.of("bookerId", userId),
                Arguments.of("ownerId", ownerId)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("allowedUserIds")
    void readById_shouldReturnDtoOfSelectedBooking(String testName, Long userId) {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        BookingDtoToClient resultBookingDto = service.readById(id, userId);

        assertThat(resultBookingDto, allOf(
                hasProperty("id", equalTo(existingBooking.getId())),
                hasProperty("start", equalTo(existingBooking.getStart())),
                hasProperty("end", equalTo(existingBooking.getEnd())),
                hasProperty("status", equalTo(existingBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(existingBooking.getItem().getId())),
                        hasProperty("name", equalTo(existingBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(existingBooking.getBooker().getId())))
        ));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approve_withNotExistingId_shouldThrowNotFoundException(boolean approved) {
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.approve(id, ownerId, approved));
        verify(repository, never()).saveAndFlush(any());
    }

    static Stream<Arguments> approveNotItemOwner() {
        return Stream.of(true, false)
                .flatMap(approved -> Stream.of(
                        Arguments.of("approved=" + approved + ", userId", approved, userId),
                        Arguments.of("approved=" + approved + ", otherUserId", approved, otherUserId)
                ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("approveNotItemOwner")
    void approve_withNotItemOwner_shouldThrowNotFoundException(String testName, boolean approved, Long userId) {
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(NotFoundException.class, () -> service.approve(id, userId, approved));
        verify(repository, never()).saveAndFlush(any());
    }

    static Stream<Arguments> approveNotWaitingStatus() {
        return Stream.of(true, false)
                .flatMap(approved -> Stream.of(BookingStatus.APPROVED, BookingStatus.REJECTED, BookingStatus.CANCELED)
                        .map(status -> Arguments.of(approved, status)));
    }

    @ParameterizedTest
    @MethodSource("approveNotWaitingStatus")
    void approve_withNotWaiting_shouldThrowBadRequestException(boolean approved, BookingStatus status) {
        existingBooking.setStatus(status);
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(BadRequestException.class, () -> service.approve(id, ownerId, approved));
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approve_withNotAvailableItem_shouldBadRequestException(boolean approved) {
        existingBooking.getItem().setAvailable(false);
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(BadRequestException.class, () -> service.approve(id, ownerId, approved));
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approve_withPastEndDate_shouldBadRequestException(boolean approved) {
        existingBooking.setStart(LocalDateTime.now().minusDays(1));
        existingBooking.setEnd(LocalDateTime.now());
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(BadRequestException.class, () -> service.approve(id, ownerId, approved));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void approve_withApprovedInReservedTime_shouldBadRequestException() {
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(1L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));

        assertThrows(BadRequestException.class, () -> service.approve(id, ownerId, true));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void approve_withRejectedInReservedTime_shouldReturnDtoOfRejectedBooking() {
        Booking updatedBooking = copyOf(existingBooking);
        updatedBooking.setStatus(BookingStatus.REJECTED);
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(1L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));
        when(repository.saveAndFlush(argThat(equalToBooking(updatedBooking)))).thenReturn(copyOf(updatedBooking));

        BookingDtoToClient resultBookingDto = service.approve(id, ownerId, false);

        assertThat(resultBookingDto, allOf(
                hasProperty("id", equalTo(updatedBooking.getId())),
                hasProperty("start", equalTo(updatedBooking.getStart())),
                hasProperty("end", equalTo(updatedBooking.getEnd())),
                hasProperty("status", equalTo(updatedBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(updatedBooking.getItem().getId())),
                        hasProperty("name", equalTo(updatedBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(updatedBooking.getBooker().getId())))
        ));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void approve_shouldReturnDtoOfBookingWithUpdatedStatus(boolean approved) {
        Booking updatedBooking = copyOf(existingBooking);
        updatedBooking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        when(repository.getApprovedBookingsCountInPeriodForItem(existingBooking.getItem().getId(),
                BookingStatus.APPROVED, existingBooking.getStart(), existingBooking.getEnd())).thenReturn(0L);
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingBooking)));
        when(repository.saveAndFlush(argThat(equalToBooking(updatedBooking)))).thenReturn(copyOf(updatedBooking));

        BookingDtoToClient resultBookingDto = service.approve(id, ownerId, approved);

        assertThat(resultBookingDto, allOf(
                hasProperty("id", equalTo(updatedBooking.getId())),
                hasProperty("start", equalTo(updatedBooking.getStart())),
                hasProperty("end", equalTo(updatedBooking.getEnd())),
                hasProperty("status", equalTo(updatedBooking.getStatus())),
                hasProperty("item", allOf(
                        hasProperty("id", equalTo(updatedBooking.getItem().getId())),
                        hasProperty("name", equalTo(updatedBooking.getItem().getName()))
                )),
                hasProperty("booker", hasProperty("id",
                        equalTo(updatedBooking.getBooker().getId())))
        ));
    }
}