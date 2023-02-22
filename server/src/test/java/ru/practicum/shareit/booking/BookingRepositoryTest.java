package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.support.ConstraintChecker;
import ru.practicum.shareit.user.User;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@Transactional
@DataJpaTest
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingRepositoryTest {
    @Autowired
    TestEntityManager em;

    @Autowired
    BookingRepository repository;

    final Long notExistingId = 1000L;

    User booker;

    User otherUser;

    User owner;

    Item item;

    Item otherItem;

    LocalDateTime now;

    static final LocalDateTime start = LocalDateTime.now().plusDays(10);

    static final LocalDateTime end = start.plusDays(60);

    static final BookingStatus status = BookingStatus.WAITING;

    final OffsetPageRequest pageRequest = OffsetPageRequest.ofOffset(1, 4,
            Sort.by("start").descending());

    @BeforeEach
    void beforeEach() {
        booker =  createUser(null, "bookerName", "booker@email.com");
        otherUser = createUser(null, "otherUserName", "other@email.com");
        owner = createUser(null, "ownerName", "owner@email.com");
        em.persist(booker);
        em.persist(otherUser);
        em.persist(owner);
        item = createItem(null, "itemName1", "itemDesc1", true, owner, null);
        otherItem = createItem(null, "itemName2", "itemDesc2", true, otherUser, null);
        em.persist(item);
        em.persist(otherItem);
        em.flush();
        now = LocalDateTime.now();
    }

    @Test
    void save_withWithNullId_shouldReturnAttachedTransferredBookingWithGeneratedId() {
        Booking booking = createBooking(null, start, end, status, item, booker);

        Booking savedBooking = repository.saveAndFlush(booking);

        assertThat(savedBooking == booking, is(true));
        assertThat(savedBooking, hasProperty("id", is(not(nullValue()))));
    }

    @Test
    void save_withWithNotExistingId_shouldReturnNewAttachedBookingWithGeneratedId() {
        Booking booking = createBooking(notExistingId, start, end, status, item, booker);

        Booking savedBooking = repository.saveAndFlush(booking);

        assertThat(booking, hasProperty("id", is(notNullValue())));
        assertThat(savedBooking == booking, is(false));
        assertThat(savedBooking, allOf(
                hasProperty("id", not(nullValue())),
                hasProperty("id", not(equalTo(booking.getId()))),
                hasProperty("start", equalTo(booking.getStart())),
                hasProperty("end", equalTo(booking.getEnd())),
                hasProperty("status", equalTo(booking.getStatus())),
                hasProperty("booker", equalTo(booking.getBooker())),
                hasProperty("item", equalTo(booking.getItem()))
        ));
    }

    static Stream<Arguments> incorrectFields() {
        return Stream.of(
                Arguments.of("null start", null, end, status),
                Arguments.of("null end", start, null, status),
                Arguments.of("null status", start, end, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("incorrectFields")
    void save_withIncorrectFields_shouldThrowException(String testName, LocalDateTime start, LocalDateTime end,
                                                              BookingStatus status) {
        Booking booking = createBooking(null, start, end, status, item, booker);

        assertThrows(Exception.class, () -> repository.saveAndFlush(booking));
    }

    @Test
    void save_withStartAfterEnd_shouldThrowException() {
        Booking booking = createBooking(null, end, start, status, item, booker);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(booking));
        assertThat(ConstraintChecker.check(exception, "ch_booking_end_date_after_start_date"),
                is(true));
    }

    @Test
    void save_withNullBooker_shouldThrowException() {
        Booking booking = createBooking(null, start, end, status, item, null);

        assertThrows(Exception.class, () -> repository.saveAndFlush(booking));
    }

    @Test
    void save_withDetachedBooker_shouldThrowException() {
        User booker = createUser(notExistingId, "newUserName", "newUser@email.com");
        Booking booking = createBooking(null, start, end, status, item, booker);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(booking));
        assertThat(ConstraintChecker.check(exception, "fk_booking_booker"), is(true));
    }

    @Test
    void save_withNullItem_shouldThrowException() {
        Booking target = createBooking(null, start, end, status, null, booker);

        assertThrows(Exception.class, () -> repository.saveAndFlush(target));
    }

    @Test
    void save_withDetachedItem_shouldThrowException() {
        Item item = createItem(notExistingId, "newItemName", "newItemDescription", true, owner,
                null);
        Booking target = createBooking(null, start, end, status, item, booker);

        Exception exception = assertThrows(Exception.class, () -> repository.saveAndFlush(target));
        assertThat(ConstraintChecker.check(exception, "fk_booking_item"), is(true));
    }

    @Test
    void update_shouldUpdateBookingFields() {
        Booking existingBooking = createBooking(null, start.minusDays(1), end.minusDays(1), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long existingId = existingBooking.getId();
        Booking booking = createBooking(existingId, start, end, BookingStatus.APPROVED, item, booker);

        Booking updatedBooking = repository.saveAndFlush(booking);

        assertThat(updatedBooking == existingBooking, is(true));
        assertThat(updatedBooking == booking, is(false));
        assertThat(updatedBooking, allOf(
                hasProperty("id", equalTo(existingId)),
                hasProperty("start", equalTo(booking.getStart())),
                hasProperty("end", equalTo(booking.getEnd())),
                hasProperty("status", equalTo(booking.getStatus())),
                hasProperty("booker", equalTo(booking.getBooker())),
                hasProperty("item", equalTo(booking.getItem()))
        ));
    }

    @Test
    void findById_withNotExistingId_shouldReturnEmptyOptional() {
        assertThat(repository.findById(notExistingId).isEmpty(), is(true));
    }

    @Test
    void findById_shouldReturnOptionalPresentedByBookingWithSuchId() {
        Booking existingBooking = createBooking(null, start, end, status, item, booker);
        em.persist(existingBooking);
        em.flush();
        Long existingId = existingBooking.getId();

        Optional<Booking> result = repository.findById(existingId);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get() == existingBooking, is(true));
    }

    @Test
    void getApprovedBookingsCountInPeriodForItem_shouldReturnCountOfApprovedBookingsForSelectedItemInSelectedPeriod() {
        Booking bookingBefore = createBooking(null, start.minusDays(2), start.minusDays(1), BookingStatus.APPROVED,
                item, booker);
        Booking booking1 = createBooking(null, start.minusDays(1), end.minusDays(1), BookingStatus.APPROVED, item,
                booker);
        Booking booking2 = createBooking(null, start.minusDays(1), end.plusDays(1), BookingStatus.APPROVED, item,
                otherUser);
        Booking booking3 = createBooking(null, start.plusDays(1), end.minusDays(1), BookingStatus.APPROVED, item,
                booker);
        Booking booking4 = createBooking(null, start.plusDays(1), end.plusDays(1), BookingStatus.APPROVED, item,
                otherUser);
        Booking bookingAfter = createBooking(null, end.plusDays(1), end.plusDays(2), BookingStatus.APPROVED, item,
                booker);
        Booking bookingWaiting = createBooking(null, start.plusDays(1), end.minusDays(1), BookingStatus.WAITING,
                item, otherUser);
        Booking bookingRejected = createBooking(null, start.plusDays(1), end.minusDays(1), BookingStatus.REJECTED,
                item, booker);
        Booking bookingCanceled = createBooking(null, start.plusDays(1), end.minusDays(1), BookingStatus.CANCELED,
                item, otherUser);
        Booking bookingOtherItem = createBooking(null, start.plusDays(1), end.minusDays(1), BookingStatus.APPROVED,
                otherItem, booker);
        em.persist(bookingBefore);
        em.persist(booking1);
        em.persist(booking2);
        em.persist(booking3);
        em.persist(booking4);
        em.persist(bookingAfter);
        em.persist(bookingWaiting);
        em.persist(bookingRejected);
        em.persist(bookingCanceled);
        em.persist(bookingOtherItem);
        Long itemId = item.getId();

        Long result = repository.getApprovedBookingsCountInPeriodForItem(itemId, BookingStatus.APPROVED, start, end);

        assertThat(result, equalTo(4L));
    }

    @Test
    void getItemBookingsCountForBooker_shouldReturnCountOfApprovedUserPastOrCurrentBookingsForSelectedItem() {
        LocalDateTime start = now.minusDays(2);
        LocalDateTime end = now.minusDays(1);
        Booking bookingPast = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED, item,
                booker);
        Booking bookingOtherItem = createBooking(null, start, end, BookingStatus.APPROVED, otherItem, booker);
        Booking bookingRejected = createBooking(null, start, end, BookingStatus.REJECTED, item, booker);
        Booking bookingWaiting = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingCanceled = createBooking(null, start, end, BookingStatus.CANCELED, item, booker);
        Booking bookingOtherUser = createBooking(null, start, end, BookingStatus.APPROVED, item, otherUser);
        Booking booking1Current = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.APPROVED, item,
                booker);
        Booking booking2Past = createBooking(null, start, end, BookingStatus.APPROVED, item, booker);
        em.persist(bookingPast);
        em.persist(bookingOtherItem);
        em.persist(bookingRejected);
        em.persist(bookingWaiting);
        em.persist(bookingCanceled);
        em.persist(bookingOtherUser);
        em.persist(booking1Current);
        em.persist(booking2Past);
        em.flush();

        Long result = repository.getItemBookingsCountForBooker(item.getId(), booker.getId(), BookingStatus.APPROVED);

        assertThat(result, equalTo(2L));
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void findFirst1ByItem_IdAndEndLessThanOrderByEndDesc_shouldReturnOptionalPresentedByLastItemBooking(
            BookingStatus status) {
        LocalDateTime end = now;
        Booking bookingFuture = createBooking(null, end.plusDays(1), end.plusDays(2), status, item, booker);
        Booking bookingCurrent = createBooking(null, end.minusDays(1), end.plusDays(1), status, item, booker);
        Booking bookingEnd1OtherItem = createBooking(null, end.minusDays(2), end.minusDays(1), status, otherItem,
                booker);
        Booking bookingEnd2 = createBooking(null, end.minusDays(3), end.minusDays(2), status, item, booker);
        Booking bookingEnd3 = createBooking(null, end.minusDays(4), end.minusDays(3), BookingStatus.REJECTED, item,
                booker);
        Booking bookingEnd4 = createBooking(null, end.minusDays(5), end.minusDays(4), BookingStatus.WAITING, item,
                booker);
        Booking bookingEnd5 = createBooking(null, end.minusDays(6), end.minusDays(5), BookingStatus.CANCELED, item,
                booker);
        Booking bookingEnd6 = createBooking(null, end.minusDays(7), end.minusDays(6), BookingStatus.APPROVED, item,
                booker);
        em.persist(bookingFuture);
        em.persist(bookingCurrent);
        em.persist(bookingEnd1OtherItem);
        em.persist(bookingEnd4);
        em.persist(bookingEnd3);
        em.persist(bookingEnd2);
        em.persist(bookingEnd5);
        em.persist(bookingEnd6);
        em.flush();

        Optional<Booking> result = repository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(item.getId(), end);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(bookingEnd2));
    }

    @Test
    void findFirst1ByItem_IdAndEndLessThanOrderByEndDesc_withNotFoundLastBooking_shouldReturnEmptyOptional() {
        LocalDateTime end = now;

        Optional<Booking> result = repository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(item.getId(), end);

        assertThat(result.isEmpty(), is(true));
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc_shouldReturnOptionalPresentedByNextItemBooking(
            BookingStatus status) {
        LocalDateTime start = LocalDateTime.now();
        Booking bookingPast = createBooking(null, start.minusDays(2), start.minusDays(1), status, item, booker);
        Booking bookingCurrent = createBooking(null, start.minusDays(1), start.plusDays(1), status, item, booker);
        Booking bookingStart1OtherItem = createBooking(null, start.plusDays(1), start.plusDays(2), status, otherItem,
                booker);
        Booking bookingStart2 = createBooking(null, start.plusDays(2), start.plusDays(3), status, item, booker);
        Booking bookingStart3 = createBooking(null, start.plusDays(3), start.plusDays(4), BookingStatus.REJECTED,
                item, booker);
        Booking bookingStart4 = createBooking(null, start.plusDays(4), start.plusDays(5), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart5 = createBooking(null, start.plusDays(5), start.plusDays(6), BookingStatus.CANCELED,
                item, booker);
        Booking bookingStart6 = createBooking(null, start.plusDays(6), start.plusDays(7), BookingStatus.APPROVED,
                item, booker);
        em.persist(bookingPast);
        em.persist(bookingCurrent);
        em.persist(bookingStart1OtherItem);
        em.persist(bookingStart3);
        em.persist(bookingStart4);
        em.persist(bookingStart2);
        em.persist(bookingStart5);
        em.persist(bookingStart6);
        em.flush();

        Optional<Booking> result =
                repository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(item.getId(), start);

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(bookingStart2));
    }

    @Test
    void findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc_withNotFoundNextBooking_shouldReturnEmptyOptional() {
        LocalDateTime start = LocalDateTime.now();

        Optional<Booking> result =
                repository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(item.getId(), start);

        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void findCurrentForBooker_shouldReturnPageOfBookerCurrentBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);
        Booking bookingPast = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingFuture = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherBooker = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                item, otherUser);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, otherItem,
                booker);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, otherItem,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                booker);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                otherItem, booker);
        em.persist(bookingPast);
        em.persist(bookingFuture);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherBooker);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long bookerId = booker.getId();

        Page<Booking> result = repository.findCurrentForBooker(bookerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findCurrentForOwner_shouldReturnPageOfOwnerCurrentBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);
        Booking bookingPast = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingFuture = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherOwner = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                otherItem, booker);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, item,
                otherUser);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, item,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                otherUser);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                item, booker);
        em.persist(bookingPast);
        em.persist(bookingFuture);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherOwner);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long ownerId = item.getOwner().getId();

        Page<Booking> result = repository.findCurrentForOwner(ownerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findPastForBooker_shouldReturnPageOfBookerPastBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(2);
        LocalDateTime end = now.minusDays(1);
        Booking bookingCurrent = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingFuture = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherBooker = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                item, otherUser);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, otherItem,
                booker);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, otherItem,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                booker);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                otherItem, booker);
        em.persist(bookingCurrent);
        em.persist(bookingFuture);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherBooker);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long bookerId = booker.getId();

        Page<Booking> result = repository.findPastForBooker(bookerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findPastForOwner_shouldReturnPageOfOwnerPastBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(2);
        LocalDateTime end = now.minusDays(1);
        Booking bookingCurrent = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingFuture = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherOwner = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                otherItem, booker);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, item,
                otherUser);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, item,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                otherUser);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                item, booker);
        em.persist(bookingCurrent);
        em.persist(bookingFuture);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherOwner);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long ownerId = item.getOwner().getId();

        Page<Booking> result = repository.findPastForOwner(ownerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findFutureForBooker_shouldReturnPageOfBookerFutureBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(2);
        Booking bookingCurrent = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingPast = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherBooker = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                item, otherUser);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, otherItem,
                booker);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, otherItem,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                booker);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                otherItem, booker);
        em.persist(bookingCurrent);
        em.persist(bookingPast);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherBooker);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long bookerId = booker.getId();

        Page<Booking> result = repository.findFutureForBooker(bookerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findFutureForOwner_shouldReturnPageOfOwnerFutureBookings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusDays(1);
        LocalDateTime end = now.plusDays(2);
        Booking bookingCurrent = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingPast = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.WAITING, item,
                booker);
        Booking bookingStart1OutOfPage = createBooking(null, start, end, BookingStatus.WAITING, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, BookingStatus.WAITING, item,
                booker);
        Booking bookingStart3OtherOwner = createBooking(null, start.minusSeconds(30), end, BookingStatus.WAITING,
                otherItem, booker);
        Booking bookingStart4 = createBooking(null, start.minusSeconds(40), end, BookingStatus.APPROVED, item,
                otherUser);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, BookingStatus.CANCELED, item,
                booker);
        Booking bookingStart6 = createBooking(null, start.minusSeconds(60), end, BookingStatus.REJECTED, item,
                otherUser);
        Booking bookingStart7OutOfPage = createBooking(null, start.minusSeconds(70), end, BookingStatus.WAITING,
                item, booker);
        em.persist(bookingCurrent);
        em.persist(bookingPast);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3OtherOwner);
        em.persist(bookingStart5);
        em.persist(bookingStart4);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long ownerId = item.getOwner().getId();

        Page<Booking> result = repository.findFutureForOwner(ownerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart4,
                bookingStart5,
                bookingStart6
        ));
    }

    static Stream<BookingStatus> bookingStatus() {
        return Stream.of(BookingStatus.WAITING, BookingStatus.REJECTED);
    }

    @ParameterizedTest
    @MethodSource("bookingStatus")
    void findByStatusForBooker_shouldReturnPageOfBookerBookingsWithSelectedStatus(BookingStatus status) {
        Booking bookingStart1OutOfPage = createBooking(null, start.minusSeconds(10), end, status, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, status, item, booker);
        Booking bookingStart3Approved = createBooking(null, start.minusSeconds(30), end, BookingStatus.APPROVED,
                item, booker);
        Booking bookingStart4Canceled = createBooking(null, start.minusSeconds(40), end, BookingStatus.CANCELED,
                item, booker);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, status, otherItem, booker);
        Booking bookingStart6OtherBooker = createBooking(null, start.minusSeconds(60), end, status, item,
                otherUser);
        Booking bookingStart7 = createBooking(null, start.minusSeconds(70), end, status, item, booker);
        Booking bookingStart8 = createBooking(null, start.minusSeconds(80), end, status, otherItem, booker);
        Booking bookingStart9OutOfPage = createBooking(null, start.minusSeconds(90), end, status, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3Approved);
        em.persist(bookingStart4Canceled);
        em.persist(bookingStart7);
        em.persist(bookingStart5);
        em.persist(bookingStart6OtherBooker);
        em.persist(bookingStart8);
        em.persist(bookingStart9OutOfPage);
        em.flush();
        Long bookerId = booker.getId();

        Page<Booking> result = repository.findByStatusForBooker(bookerId, status, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart5,
                bookingStart7,
                bookingStart8
        ));
    }

    @ParameterizedTest
    @MethodSource("bookingStatus")
    void findByStatusForOwner_shouldReturnPageOfOwnerBookingsWithSelectedStatus(BookingStatus status) {
        Booking bookingStart1OutOfPage = createBooking(null, start.minusSeconds(10), end, status, item, booker);
        Booking bookingStart2 = createBooking(null, start.minusSeconds(20), end, status, item, booker);
        Booking bookingStart3Approved = createBooking(null, start.minusSeconds(30), end, BookingStatus.APPROVED,
                item, booker);
        Booking bookingStart4Canceled = createBooking(null, start.minusSeconds(40), end, BookingStatus.CANCELED,
                item, booker);
        Booking bookingStart5 = createBooking(null, start.minusSeconds(50), end, status, item, otherUser);
        Booking bookingStart6OtherOwner = createBooking(null, start.minusSeconds(60), end, status, otherItem,
                booker);
        Booking bookingStart7 = createBooking(null, start.minusSeconds(70), end, status, item, booker);
        Booking bookingStart8 = createBooking(null, start.minusSeconds(80), end, status, item, otherUser);
        Booking bookingStart9OutOfPage = createBooking(null, start.minusSeconds(90), end, status, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart3Approved);
        em.persist(bookingStart4Canceled);
        em.persist(bookingStart7);
        em.persist(bookingStart5);
        em.persist(bookingStart6OtherOwner);
        em.persist(bookingStart8);
        em.persist(bookingStart9OutOfPage);
        em.flush();
        Long ownerId = item.getOwner().getId();

        Page<Booking> result = repository.findByStatusForOwner(ownerId, status, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart5,
                bookingStart7,
                bookingStart8
        ));
    }

    @Test
    void findAllForBooker_shouldReturnPageOfBookerBookings() {
        Booking bookingStart1OutOfPage = createBooking(null, now.plusDays(4), now.plusDays(5), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart2 = createBooking(null, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart3 =  createBooking(null, now.plusDays(2), now.plusDays(3), BookingStatus.APPROVED,
                otherItem, booker);
        Booking bookingStart4OtherBooker = createBooking(null, now.plusDays(1), now.plusDays(2),
                BookingStatus.WAITING, item, otherUser);
        Booking bookingStart5 = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.REJECTED,
                item, booker);
        Booking bookingStart6 = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.CANCELED,
                otherItem, booker);
        Booking bookingStart7OutOfPage = createBooking(null, now.minusDays(3), now.minusDays(2),
                BookingStatus.CANCELED, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart5);
        em.persist(bookingStart3);
        em.persist(bookingStart4OtherBooker);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long bookerId = booker.getId();

        Page<Booking> result = repository.findAllForBooker(bookerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart3,
                bookingStart5,
                bookingStart6
        ));
    }

    @Test
    void findAllForOwner_shouldReturnPageOfOwnerBookings() {
        Booking bookingStart1OutOfPage = createBooking(null, now.plusDays(4), now.plusDays(5), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart2 = createBooking(null, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart3 =  createBooking(null, now.plusDays(2), now.plusDays(3), BookingStatus.APPROVED,
                item, otherUser);
        Booking bookingStart4OtherOwner = createBooking(null, now.plusDays(1), now.plusDays(2),
                BookingStatus.WAITING, otherItem, booker);
        Booking bookingStart5 = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.REJECTED,
                item, booker);
        Booking bookingStart6 = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.CANCELED,
                item, otherUser);
        Booking bookingStart7OutOfPage = createBooking(null, now.minusDays(3), now.minusDays(2),
                BookingStatus.CANCELED, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart2);
        em.persist(bookingStart5);
        em.persist(bookingStart3);
        em.persist(bookingStart4OtherOwner);
        em.persist(bookingStart6);
        em.persist(bookingStart7OutOfPage);
        em.flush();
        Long ownerId = item.getOwner().getId();

        Page<Booking> result = repository.findAllForOwner(ownerId, pageRequest);

        assertThat(result, contains(
                bookingStart2,
                bookingStart3,
                bookingStart5,
                bookingStart6
        ));
    }
}