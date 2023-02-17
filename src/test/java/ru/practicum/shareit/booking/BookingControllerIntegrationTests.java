package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingDtoToClient;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.common.ControllerErrorHandler;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBookingDtoFromClient;
import static ru.practicum.shareit.tools.factories.ItemFactory.createItem;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;

@Transactional
@SpringBootTest
@AutoConfigureTestDatabase
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingControllerIntegrationTests {
    MockMvc mockMvc;

    @Autowired
    BookingController controller;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ControllerErrorHandler controllerErrorHandler;

    final Long notExistingId = 1000L;

    User owner;

    User booker;

    User otherUser;

    Item item;

    Item otherItem;

    BookingDtoFromClient requestBookingDto;

    LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(controllerErrorHandler)
                .build();
        owner = createUser(null, "ownerName", "owner@email.com");
        booker = createUser(null, "bookerName", "booker@email.com");
        otherUser = createUser(null, "otherUserName", "otherUser@email.com");
        em.persist(owner);
        em.persist(booker);
        em.persist(otherUser);
        item = createItem(null, "item1Name", "item1Description", true,
                owner, null);
        otherItem = createItem(null, "item2Name", "item2Description", true,
                otherUser, null);
        em.persist(item);
        em.persist(otherItem);
        em.flush();
        now = LocalDateTime.now();
        requestBookingDto = createBookingDtoFromClient(item.getId(), now.plusDays(1), now.plusDays(2));
    }

    @Test
    void create_withStartNotBeforeEnd_shouldReturnStatusBadRequest() throws Exception {
        requestBookingDto.setStart(now.plusDays(2));
        requestBookingDto.setEnd(now.plusDays(1));
        Long userId = booker.getId();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withPeriodWithReservedBookings_shouldReturnStatusBadRequest() throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(10), BookingStatus.APPROVED,
                item, otherUser);
        em.persist(existingBooking);
        requestBookingDto.setStart(now.plusDays(2));
        requestBookingDto.setEnd(now.plusDays(3));
        Long userId = booker.getId();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withOwner_shouldReturnStatusNotFound() throws Exception {
        Long userId = item.getOwner().getId();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withNotAvailableItem_shouldReturnStatusBadRequest() throws Exception {
        item.setAvailable(false);
        em.persist(item);
        em.flush();
        Long userId = booker.getId();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withNotExistingBooker_shouldReturnStatusNotFound() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", notExistingId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturnStatusOkAndDtoOfCreatedBooking() throws Exception {
        Long userId = booker.getId();

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(objectMapper.writeValueAsString(requestBookingDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(notNullValue())),
                        jsonPath("$.status", equalTo(BookingStatus.WAITING.toString())),
                        jsonPath("$.start", equalTo(requestBookingDto.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.end", equalTo(requestBookingDto.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.item.id", equalTo(item.getId()), Long.class),
                        jsonPath("$.item.name", equalTo(item.getName())),
                        jsonPath("$.booker.id", equalTo(userId), Long.class));

        Booking createdBooking = em.createQuery("Select b from Booking b", Booking.class).getSingleResult();

        assertThat(createdBooking, allOf(
                hasProperty("id", is(notNullValue())),
                hasProperty("start", equalTo(requestBookingDto.getStart())),
                hasProperty("end", equalTo(requestBookingDto.getEnd())),
                hasProperty("status", equalTo(BookingStatus.WAITING)),
                hasProperty("item", equalTo(item)),
                hasProperty("booker", equalTo(booker))
        ));
    }

    @Test
    void readByBooker_withNoBookings_shouldReturnStatusNotFound() throws Exception {
        Long userId = booker.getId();

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readByBooker_shouldReturnStatusOkAndDtoListOfBookerBookingsSortedByStartWithPagination() throws Exception {
        Booking bookingStart1OutOfPage = createBooking(null, now.plusDays(4), now.plusDays(5), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart2 = createBooking(null, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart3OtherBooker = createBooking(null, now.plusDays(1), now.plusDays(2),
                BookingStatus.WAITING, item, otherUser);
        Booking bookingStart4 = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.REJECTED,
                otherItem, booker);
        Booking bookingStart5OutOfPage = createBooking(null, now.minusDays(3), now.minusDays(2),
                BookingStatus.CANCELED, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart4);
        em.persist(bookingStart3OtherBooker);
        em.persist(bookingStart2);
        em.persist(bookingStart5OutOfPage);
        em.flush();
        Long userId = booker.getId();
        int from = 1;
        int size = 2;

        mockMvc.perform(get("/bookings?from=" + from + "&size=" + size)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(bookingStart2.getId()), Long.class),
                        jsonPath("$[0].status", equalTo(bookingStart2.getStatus().toString())),
                        jsonPath("$[0].start", equalTo(bookingStart2.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].end", equalTo(bookingStart2.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].item.id", equalTo(bookingStart2.getItem().getId()), Long.class),
                        jsonPath("$[0].item.name", equalTo(bookingStart2.getItem().getName())),
                        jsonPath("$[0].booker.id", equalTo(bookingStart2.getBooker().getId()), Long.class),
                        jsonPath("$[1].id", equalTo(bookingStart4.getId()), Long.class),
                        jsonPath("$[1].status", equalTo(bookingStart4.getStatus().toString())),
                        jsonPath("$[1].start", equalTo(bookingStart4.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].end", equalTo(bookingStart4.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].item.id", equalTo(bookingStart4.getItem().getId()), Long.class),
                        jsonPath("$[1].item.name", equalTo(bookingStart4.getItem().getName())),
                        jsonPath("$[1].booker.id", equalTo(bookingStart4.getBooker().getId()), Long.class)
                );
    }

    @Test
    void readByOwner_shouldReturnStatusOkAndDtoListOfOwnerItemsBookingsSortedByStartWithPagination() throws Exception {
        Booking bookingStart1OutOfPage = createBooking(null, now.plusDays(4), now.plusDays(5), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart2 = createBooking(null, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING,
                item, booker);
        Booking bookingStart3OtherOwner = createBooking(null, now.plusDays(1), now.plusDays(2),
                BookingStatus.WAITING, otherItem, booker);
        Booking bookingStart4 = createBooking(null, now.minusDays(1), now.plusDays(1), BookingStatus.REJECTED,
                item, otherUser);
        Booking bookingStart5OutOfPage = createBooking(null, now.minusDays(3), now.minusDays(2),
                BookingStatus.CANCELED, item, booker);
        em.persist(bookingStart1OutOfPage);
        em.persist(bookingStart4);
        em.persist(bookingStart3OtherOwner);
        em.persist(bookingStart2);
        em.persist(bookingStart5OutOfPage);
        em.flush();
        Long userId = item.getOwner().getId();
        int from = 1;
        int size = 2;

        mockMvc.perform(get("/bookings/owner?from=" + from + "&size=" + size)
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id", equalTo(bookingStart2.getId()), Long.class),
                        jsonPath("$[0].status", equalTo(bookingStart2.getStatus().toString())),
                        jsonPath("$[0].start", equalTo(bookingStart2.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].end", equalTo(bookingStart2.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[0].item.id", equalTo(bookingStart2.getItem().getId()), Long.class),
                        jsonPath("$[0].item.name", equalTo(bookingStart2.getItem().getName())),
                        jsonPath("$[0].booker.id", equalTo(bookingStart2.getBooker().getId()), Long.class),
                        jsonPath("$[1].id", equalTo(bookingStart4.getId()), Long.class),
                        jsonPath("$[1].status", equalTo(bookingStart4.getStatus().toString())),
                        jsonPath("$[1].start", equalTo(bookingStart4.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].end", equalTo(bookingStart4.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$[1].item.id", equalTo(bookingStart4.getItem().getId()), Long.class),
                        jsonPath("$[1].item.name", equalTo(bookingStart4.getItem().getName())),
                        jsonPath("$[1].booker.id", equalTo(bookingStart4.getBooker().getId()), Long.class)
                );
    }

    @Test
    void readById_withNotExistingBooking_shouldReturnStatusNotFound() throws Exception {
        Long userId = booker.getId();

        mockMvc.perform(get("/bookings/" + notExistingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_withNotBookerOrOwner_shouldReturnStatusNotFound() throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = otherUser.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(get("/bookings/" + existingId).header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void readById_shouldReturnStatusOkAndDtoOfSelectedBooking() throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = booker.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(get("/bookings/" + existingId).header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingBooking.getId()), Long.class),
                        jsonPath("$.status", equalTo(existingBooking.getStatus().toString())),
                        jsonPath("$.start", equalTo(existingBooking.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.end", equalTo(existingBooking.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.item.id", equalTo(existingBooking.getItem().getId()), Long.class),
                        jsonPath("$.item.name", equalTo(existingBooking.getItem().getName())),
                        jsonPath("$.booker.id", equalTo(existingBooking.getBooker().getId()), Long.class)
                );
    }

    @Test
    void approve_withNotExistingBooking_shouldReturnStatusNotFound() throws Exception {
        Long userId = owner.getId();

        mockMvc.perform(patch("/bookings/" + notExistingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_withNotOwner_shouldReturnStatusNotFound() throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = booker.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void approve_withNotWaitingStatus_shouldReturnStatusBadRequest() throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = owner.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_withNotAvailableItem_shouldReturnStatusBadRequest() throws Exception {
        item.setAvailable(false);
        em.persist(item);
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = owner.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_withPastBooking_shouldReturnStatusBadRequest() throws Exception {
        Booking existingBooking = createBooking(null, now.minusDays(2), now.minusDays(1), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = owner.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_withApprovingInReservedTime_shouldReturnStatusBadRequest() throws Exception {
        Booking approvedBooking = createBooking(null, now.plusDays(1), now.plusDays(10), BookingStatus.APPROVED,
                item, otherUser);
        em.persist(approvedBooking);
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = owner.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=true")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isBadRequest());
    }

    private static Stream<BookingStatus> approve() {
        return Stream.of(BookingStatus.APPROVED, BookingStatus.REJECTED);
    }

    @ParameterizedTest
    @MethodSource("approve")
    void approve_shouldReturnStatusOkAndDtoOfApprovedOrRejectedBooking(BookingStatus status) throws Exception {
        Booking existingBooking = createBooking(null, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING,
                item, booker);
        em.persist(existingBooking);
        em.flush();
        Long userId = owner.getId();
        Long existingId = existingBooking.getId();

        mockMvc.perform(patch("/bookings/" + existingId + "?approved=" + (status == BookingStatus.APPROVED))
                        .header("X-Sharer-User-Id", userId))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", equalTo(existingBooking.getId()), Long.class),
                        jsonPath("$.status", equalTo(status.toString())),
                        jsonPath("$.start", equalTo(existingBooking.getStart()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.end", equalTo(existingBooking.getEnd()
                                .format(DateTimeFormatter.ofPattern(BookingDtoToClient.DATE_PATTERN)))),
                        jsonPath("$.item.id", equalTo(existingBooking.getItem().getId()), Long.class),
                        jsonPath("$.item.name", equalTo(existingBooking.getItem().getName())),
                        jsonPath("$.booker.id", equalTo(existingBooking.getBooker().getId()), Long.class)
                );

        assertThat(em.contains(existingBooking), is(true));
        assertThat(existingBooking, allOf(
                hasProperty("id", equalTo(existingBooking.getId())),
                hasProperty("start", equalTo(existingBooking.getStart())),
                hasProperty("end", equalTo(existingBooking.getEnd())),
                hasProperty("status", equalTo(status)),
                hasProperty("item", equalTo(existingBooking.getItem())),
                hasProperty("booker", equalTo(existingBooking.getBooker()))
        ));
    }
}