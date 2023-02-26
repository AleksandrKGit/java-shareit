package ru.practicum.shareit.item.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.support.OffsetPageRequest;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import ru.practicum.shareit.tools.factories.BookingFactory;
import ru.practicum.shareit.tools.factories.ItemRequestFactory;
import ru.practicum.shareit.tools.factories.UserFactory;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.tools.factories.BookingFactory.createBooking;
import static ru.practicum.shareit.tools.factories.ItemFactory.*;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.createItemRequest;
import static ru.practicum.shareit.tools.factories.UserFactory.createUser;
import static ru.practicum.shareit.tools.matchers.DateMatcher.near;
import static ru.practicum.shareit.tools.matchers.ItemMatcher.equalToItem;

@SpringBootTest(classes = {ItemServiceImpl.class, ItemMapperImpl.class, CommentMapperImpl.class})
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ItemServiceTest {
    @Autowired
    ItemService service;

    @MockBean
    ItemRepository repository;

    @MockBean
    ItemRequestRepository itemRequestRepository;

    @MockBean
    UserRepository userRepository;

    @MockBean
    BookingRepository bookingRepository;

    @MockBean
    CommentRepository commentRepository;

    ItemDtoFromClient requestItemDto;

    CommentDtoFromClient requestCommentDto;

    final Long id = 1L;

    Item createdItem;

    Item existingItem;

    Item updatedItem;

    Item otherUserItem;

    final Long userId = 2L;

    User user;

    final Long otherUserId = 3L;

    final Long requestId = 4L;

    ItemRequest request;

    Comment comment;

    final int from = 5;

    final int size = 15;

    Sort sort;

    Booking last;

    Booking next;

    @BeforeEach
    void setUp() {
        requestItemDto = createItemDtoFromClient("itemName", "itemDescription", true,
                requestId);
        requestCommentDto = createCommentDtoFromClient("commentText");
        user = createUser(userId, "userName", null);
        request = createItemRequest(requestId, null, null, null);
        createdItem = createItem(id, requestItemDto.getName(), requestItemDto.getDescription(),
                requestItemDto.getAvailable(), user, request);
        existingItem = createItem(id, "existingItemName", "existingItemDescription", false, user,
                request);
        updatedItem = copyOf(createdItem);
        comment = createComment(5L, requestCommentDto.getText(), LocalDateTime.now(), user, createdItem);
        sort = Sort.by("id").ascending();
        last = createBooking(6L, null, null, null, null,
                createUser(7L, null, null));
        next = createBooking(8L, null, null, null, null,
                createUser(9L, null, null));
        otherUserItem = createItem(10L, "otherName", "otherDescription", false,
                createUser(otherUserId, null, null), null);
    }

    @Test
    void create_withNotExistingUser_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(userId, requestItemDto));
        verify(itemRequestRepository, never()).findById(any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withNotExistingRequestId_shouldThrowNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(userId, requestItemDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void create_withNullRequestId_shouldReturnDtoOfCreatedWithNullRequestItem() {
        createdItem.setRequest(null);
        requestItemDto.setRequestId(null);
        ArgumentCaptor<Item> itemArgumentCaptor = ArgumentCaptor.forClass(Item.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(repository.saveAndFlush(itemArgumentCaptor.capture())).thenReturn(copyOf(createdItem));

        ItemDtoToClient resultItemDto = service.create(userId, requestItemDto);
        Item itemToRepository = itemArgumentCaptor.getValue();

        assertThat(itemToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(requestItemDto.getName())),
                hasProperty("description", equalTo(requestItemDto.getDescription())),
                hasProperty("available", equalTo(requestItemDto.getAvailable())),
                hasProperty("request", is(nullValue())),
                hasProperty("owner", hasProperty("id", equalTo(user.getId())))
        ));

        assertThat(resultItemDto, allOf(
                hasProperty("id", equalTo(createdItem.getId())),
                hasProperty("requestId", is(nullValue())),
                hasProperty("name", equalTo(createdItem.getName())),
                hasProperty("description", equalTo(createdItem.getDescription())),
                hasProperty("available", equalTo(createdItem.getAvailable())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", is(nullValue()))
        ));

        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void create_withNotNullRequestId_shouldReturnDtoOfCreatedWithRequestItem() {
        ArgumentCaptor<Item> itemArgumentCaptor = ArgumentCaptor.forClass(Item.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(ItemRequestFactory.copyOf(request)));
        when(repository.saveAndFlush(itemArgumentCaptor.capture())).thenReturn(copyOf(createdItem));

        ItemDtoToClient resultItemDto = service.create(userId, requestItemDto);
        Item itemToRepository = itemArgumentCaptor.getValue();

        assertThat(itemToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("name", equalTo(requestItemDto.getName())),
                hasProperty("description", equalTo(requestItemDto.getDescription())),
                hasProperty("available", equalTo(requestItemDto.getAvailable())),
                hasProperty("request", hasProperty("id", equalTo(request.getId()))),
                hasProperty("owner", hasProperty("id", equalTo(user.getId())))
        ));

        assertThat(resultItemDto, allOf(
                hasProperty("id", equalTo(createdItem.getId())),
                hasProperty("requestId", equalTo(createdItem.getRequest().getId())),
                hasProperty("name", equalTo(createdItem.getName())),
                hasProperty("description", equalTo(createdItem.getDescription())),
                hasProperty("available", equalTo(createdItem.getAvailable())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", is(nullValue()))
        ));
    }

    @Test
    void createComment_withNotBookedItem_shouldThrowBadRequestException() {
        when(bookingRepository.getItemBookingsCountForBooker(id, userId, BookingStatus.APPROVED)).thenReturn(0L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));

        assertThrows(BadRequestException.class, () -> service.createComment(userId, id, requestCommentDto));
        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createComment_withDeletedAuthor_shouldThrowNotFoundException() {
        when(bookingRepository.getItemBookingsCountForBooker(id, userId, BookingStatus.APPROVED)).thenReturn(1L);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.createComment(userId, id, requestCommentDto));
        verify(commentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createComment_shouldReturnDtoOfCreatedComment() {
        when(bookingRepository.getItemBookingsCountForBooker(id, userId, BookingStatus.APPROVED)).thenReturn(1L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(UserFactory.copyOf(user)));
        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);
        when(commentRepository.saveAndFlush(commentArgumentCaptor.capture())).thenReturn(copyOf(comment));

        CommentDtoToClient resultCommentDto = service.createComment(userId, id, requestCommentDto);
        Comment commentToRepository = commentArgumentCaptor.getValue();

        assertThat(commentToRepository, allOf(
                hasProperty("id", is(nullValue())),
                hasProperty("text", equalTo(requestCommentDto.getText())),
                hasProperty("created", lessThanOrEqualTo(LocalDateTime.now().plusSeconds(2))),
                hasProperty("created", greaterThan(LocalDateTime.now().minusSeconds(2))),
                hasProperty("item", hasProperty("id", equalTo(id))),
                hasProperty("author", hasProperty("id", equalTo(user.getId())))
        ));

        assertThat(resultCommentDto, allOf(
                hasProperty("id", equalTo(comment.getId())),
                hasProperty("text", equalTo(comment.getText())),
                hasProperty("created", equalTo(comment.getCreated())),
                hasProperty("authorName", equalTo(user.getName()))
        ));
    }

    @Test
    void readByOwner_shouldReturnDtoListOfOwnerItemsWithLastAndNextBookingAndComments() {
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.ofOffset(from, size, sort);
        when(repository.findByOwner_Id(userId, offsetPageRequest))
                .thenReturn(new PageImpl<>(List.of(copyOf(existingItem))));
        when(bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(last)));
        when(bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(next)));
        when(commentRepository.findByItem_IdOrderByCreatedDesc(existingItem.getId()))
                .thenReturn(List.of(copyOf(comment)));

        List<ItemDtoToClient> resultItemDtoList = service.readByOwner(userId, from, size);

        assertThat(resultItemDtoList, contains(allOf(
                hasProperty("id", equalTo(existingItem.getId())),
                hasProperty("requestId", equalTo(existingItem.getRequest().getId())),
                hasProperty("name", equalTo(existingItem.getName())),
                hasProperty("description", equalTo(existingItem.getDescription())),
                hasProperty("available", equalTo(existingItem.getAvailable())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(last.getId())),
                        hasProperty("bookerId", equalTo(last.getBooker().getId()))
                )),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(next.getId())),
                        hasProperty("bookerId", equalTo(next.getBooker().getId()))
                )),
                hasProperty("comments", contains(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        )));
    }

    @Test
    void readByQuery_shouldReturnDtoListIfItemsFoundByQueryWithoutCommentsAndWithLastAndNextBookingForOwnerItems() {
        String query = "query";
        OffsetPageRequest offsetPageRequest = OffsetPageRequest.ofOffset(from, size, sort);
        when(repository.findByQuery(query, offsetPageRequest)).thenReturn(new PageImpl<>(List.of(copyOf(existingItem),
                copyOf(otherUserItem))));
        when(bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(last)));
        when(bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(next)));

        List<ItemDtoToClient> resultItemDtoList = service.readByQuery(userId, query, from, size);

        assertThat(resultItemDtoList, contains(
                allOf(
                        hasProperty("id", equalTo(existingItem.getId())),
                        hasProperty("requestId", equalTo(existingItem.getRequest().getId())),
                        hasProperty("name", equalTo(existingItem.getName())),
                        hasProperty("description", equalTo(existingItem.getDescription())),
                        hasProperty("available", equalTo(existingItem.getAvailable())),
                        hasProperty("lastBooking", allOf(
                                hasProperty("id", equalTo(last.getId())),
                                hasProperty("bookerId", equalTo(last.getBooker().getId()))
                        )),
                        hasProperty("nextBooking", allOf(
                                hasProperty("id", equalTo(next.getId())),
                                hasProperty("bookerId", equalTo(next.getBooker().getId()))
                        )),
                        hasProperty("comments", is(nullValue()))
                ), allOf(
                        hasProperty("id", equalTo(otherUserItem.getId())),
                        hasProperty("requestId", is(nullValue())),
                        hasProperty("name", equalTo(otherUserItem.getName())),
                        hasProperty("description", equalTo(otherUserItem.getDescription())),
                        hasProperty("available", equalTo(otherUserItem.getAvailable())),
                        hasProperty("lastBooking", is(nullValue())),
                        hasProperty("nextBooking", is(nullValue())),
                        hasProperty("comments", is(nullValue()))
                )
        ));

        verify(commentRepository, never()).findByItem_IdOrderByCreatedDesc(any());

        verify(bookingRepository, never())
                .findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(otherUserItem.getId()), any());

        verify(bookingRepository, never())
                .findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(otherUserItem.getId()), any());
    }

    @Test
    void readById_withNotExistingId_shouldThrowNotFoundException() {
        when(repository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.readById(userId, id));
    }

    @Test
    void readById_withOwner_shouldReturnDtoOfItemWithLastAndNextBookingAndComments() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingItem)));
        when(bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(last)));
        when(bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(existingItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(next)));
        when(commentRepository.findByItem_IdOrderByCreatedDesc(existingItem.getId()))
                .thenReturn(List.of(copyOf(comment)));

        ItemDtoToClient resultItemDtoList = service.readById(userId, id);

        assertThat(resultItemDtoList, allOf(
                hasProperty("id", equalTo(existingItem.getId())),
                hasProperty("requestId", equalTo(existingItem.getRequest().getId())),
                hasProperty("name", equalTo(existingItem.getName())),
                hasProperty("description", equalTo(existingItem.getDescription())),
                hasProperty("available", equalTo(existingItem.getAvailable())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(last.getId())),
                        hasProperty("bookerId", equalTo(last.getBooker().getId()))
                )),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(next.getId())),
                        hasProperty("bookerId", equalTo(next.getBooker().getId()))
                )),
                hasProperty("comments", contains(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        ));
    }

    @Test
    void readById_withOtherUser_shouldReturnDtoOfItemWithoutLastAndNextBookingAndWithComments() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingItem)));
        when(commentRepository.findByItem_IdOrderByCreatedDesc(existingItem.getId()))
                .thenReturn(List.of(copyOf(comment)));

        ItemDtoToClient resultItemDto = service.readById(otherUserId, id);

        assertThat(resultItemDto, allOf(
                hasProperty("id", equalTo(existingItem.getId())),
                hasProperty("requestId", equalTo(existingItem.getRequest().getId())),
                hasProperty("name", equalTo(existingItem.getName())),
                hasProperty("description", equalTo(existingItem.getDescription())),
                hasProperty("available", equalTo(existingItem.getAvailable())),
                hasProperty("lastBooking", is(nullValue())),
                hasProperty("nextBooking", is(nullValue())),
                hasProperty("comments", contains(allOf(
                        hasProperty("id", equalTo(comment.getId())),
                        hasProperty("text", equalTo(comment.getText())),
                        hasProperty("authorName", equalTo(comment.getAuthor().getName())),
                        hasProperty("created", equalTo(comment.getCreated()))
                )))
        ));

        verify(bookingRepository, never()).findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(createdItem.getId()),
                argThat(near(LocalDateTime.now())));

        verify(bookingRepository, never()).findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(
                eq(createdItem.getId()), argThat(near(LocalDateTime.now())));
    }

    @Test
    void update_withNotExistingId_shouldThrowNotFoundException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(userId, id, requestItemDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void update_withNotUserItem_shouldThrowAccessDeniedException() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingItem)));

        assertThrows(AccessDeniedException.class, () -> service.update(otherUserId, id, requestItemDto));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void update_shouldReturnDtoOfUpdatedItemWithLastAndNextBookingAndWithoutComments() {
        when(repository.findById(id)).thenReturn(Optional.of(copyOf(existingItem)));
        when(repository.saveAndFlush(argThat(equalToItem(updatedItem)))).thenReturn(copyOf(updatedItem));
        when(bookingRepository.findFirst1ByItem_IdAndEndLessThanOrderByEndDesc(eq(updatedItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(last)));
        when(bookingRepository.findFirst1ByItem_IdAndStartGreaterThanOrderByStartAsc(eq(updatedItem.getId()),
                argThat(near(LocalDateTime.now())))).thenReturn(Optional.of(BookingFactory.copyOf(next)));

        ItemDtoToClient resultItemDto = service.update(userId, id, requestItemDto);

        assertThat(resultItemDto, allOf(
                hasProperty("id", equalTo(updatedItem.getId())),
                hasProperty("requestId", equalTo(updatedItem.getRequest().getId())),
                hasProperty("name", equalTo(updatedItem.getName())),
                hasProperty("description", equalTo(updatedItem.getDescription())),
                hasProperty("available", equalTo(updatedItem.getAvailable())),
                hasProperty("lastBooking", allOf(
                        hasProperty("id", equalTo(last.getId())),
                        hasProperty("bookerId", equalTo(last.getBooker().getId()))
                )),
                hasProperty("nextBooking", allOf(
                        hasProperty("id", equalTo(next.getId())),
                        hasProperty("bookerId", equalTo(next.getBooker().getId()))
                )),
                hasProperty("comments", is(nullValue()))
        ));

        verify(commentRepository, never()).findByItem_IdOrderByCreatedDesc(updatedItem.getId());
    }
}