package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;
import static ru.practicum.shareit.tools.matchers.DateMatcher.near;

@SpringBootTest(classes = {ItemMapperImpl.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemMapperTest {
    @Autowired
    ItemMapper itemMapper;

    BookingRepository bookingRepository;

    CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
    }

    @Test
    void toDto_withNotNullFieldsAndRepositories_shouldReturnDtoWithNotNullFields() {
        Item source = createItem(10L, "itemName", "itemDescription", true,
                createUser(20L, null, null),
                createItemRequest(30L, null, null, null));
        Booking last = createBooking(40L, null, null, null, null,
                createUser(50L, null, null));
        Booking next = createBooking(60L, null, null, null, null,
                createUser(70L, null, null));
        LocalDateTime now = LocalDateTime.now();
        List<Comment> comments = List.of(createComment(80L, "text", now.minusDays(1),
                createUser(null, "author", null), null));
        when(bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(source.getId()), argThat(near(now))))
                .thenReturn(Optional.of(last));
        when(bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(source.getId()),
                argThat(near(now)))).thenReturn(Optional.of(next));
        when(commentRepository.findByItem_IdOrderByCreatedDesc(source.getId())).thenReturn(comments);

        ItemDtoToClient target = itemMapper.toDto(source, source.getOwner().getId(), bookingRepository,
                commentRepository);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(last.getId())),
                        hasProperty("bookerId", equalTo(last.getBooker().getId()))
                )),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(next.getId())),
                        hasProperty("bookerId", equalTo(next.getBooker().getId()))
                )),
                hasProperty("comments", contains(allOf(
                        hasProperty("id", equalTo(comments.get(0).getId())),
                        hasProperty("text", equalTo(comments.get(0).getText())),
                        hasProperty("created", equalTo(comments.get(0).getCreated())),
                        hasProperty("authorName", equalTo(comments.get(0).getAuthor().getName()))
                ))),
                hasProperty("requestId", equalTo(source.getRequest().getId()))
        ));
    }

    @Test
    void toDto_withNullAndNullUserIdAndRepositories_shouldReturnNull() {
        assertThat(itemMapper.toDto(null, null, null, null), is(nullValue()));
    }

    @Test
    void toDto_withNullFieldsAndUserIdAndRepositories_shouldReturnDtoWithNullFields() {
        Item source = createItem(null, null, null, null, null, null);

        ItemDtoToClient target = itemMapper.toDto(source, null, null, null);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }

    @Test
    void toBookingDto_withNotNullFields_shouldReturnDtoWithNotNullFields() {
        Booking source = createBooking(10L, null, null, null, null,
                createUser(20L, null, null));

        BookingDtoToClient target = itemMapper.toBookingDto(source);

        assertThat(target, allOf(
                hasProperty("id", equalTo(source.getId())),
                hasProperty("bookerId", equalTo(source.getBooker().getId()))
        ));
    }

    @Test
    void toBookingDto_withNull_shouldReturnNull() {
        assertThat(itemMapper.toBookingDto(null), is(nullValue()));
    }

    @Test
    void toBookingDto_withNullFields_shouldReturnDtoWithNullFields() {
        Booking source = createBooking(null, null, null, null, null,
                createUser(null, null, null));

        BookingDtoToClient target = itemMapper.toBookingDto(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("bookerId", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNotNullFields_shouldReturnEntityWithNotNullFields() {
        ItemDtoFromClient source = createItemDtoFromClient("itemName", "itemDescription", true,
                20L);

        Item target = itemMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("owner", is(nullValue())),
                hasProperty("request", is(nullValue()))
        ));
    }

    @Test
    void toEntity_withNull_shouldReturnNull() {
        assertThat(itemMapper.toEntity(null), nullValue());
    }

    @Test
    void toEntity_withNullFields_shouldReturnEntityWithNullFields() {
        ItemDtoFromClient source = createItemDtoFromClient(null, null, null, null);

        Item target = itemMapper.toEntity(source);

        assertThat(target, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("owner", is(nullValue())),
                hasProperty("request", is(nullValue()))
        ));
    }

    @Test
    void updateEntityFromDto_withNullFields_shouldNotUpdateEntity() {
        ItemDtoFromClient source = createItemDtoFromClient(null, null, null, null);
        Item target = createItem(10L, "itemName", "itemDescription", true, null,
                createItemRequest(20L, null, null, null));

        itemMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", is(not(nullValue()))),
                hasProperty("description", is(not(nullValue()))),
                hasProperty("available", is(not(nullValue()))),
                hasProperty("request", hasProperty("id", is(not(nullValue()))))
        ));
    }

    @Test
    void updateEntityFromDto_withNotNullFields_shouldUpdateEntity() {
        ItemDtoFromClient source = createItemDtoFromClient("newName", "newDescription", false,
                30L);
        Item target = createItem(10L, "itemName", "itemDescription", true, null,
                createItemRequest(20L, null, null, null));

        itemMapper.updateEntityFromDto(source, target);

        assertThat(target, allOf(
                hasProperty("id", is(not(nullValue()))),
                hasProperty("name", equalTo(source.getName())),
                hasProperty("description", equalTo(source.getDescription())),
                hasProperty("available", equalTo(source.getAvailable())),
                hasProperty("request",
                        hasProperty("id", is(not(equalTo(source.getRequestId())))))
        ));
    }
}