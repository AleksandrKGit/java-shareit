package ru.practicum.shareit.tools.factories;

import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.List;

public class ItemFactory {
    public static ItemDtoFromClient createItemDtoFromClient(String name, String description, Boolean available,
                                                            Long requestId) {
        ItemDtoFromClient itemDto = new ItemDtoFromClient();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    public static ItemDtoToClient createItemDtoToClient(Long id, String name, String description, Boolean available,
                                                        Long requestId, BookingDtoToClient last,
                                                        BookingDtoToClient next, List<CommentDtoToClient> comments) {
        ItemDtoToClient itemDto = new ItemDtoToClient();
        itemDto.setId(id);
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        itemDto.setLastBooking(last);
        itemDto.setNextBooking(next);
        itemDto.setComments(comments);
        return itemDto;
    }

    public static Item copyOf(Item item) {
        if (item == null) {
            return null;
        }

        Item copy = new Item();
        copy.setId(item.getId());
        copy.setName(item.getName());
        copy.setDescription(item.getDescription());
        copy.setAvailable(item.getAvailable());
        copy.setOwner(UserFactory.copyOf(item.getOwner()));
        copy.setRequest(ItemRequestFactory.copyOf(item.getRequest()));
        return copy;
    }

    public static Item createItem(Long id, String name, String description, Boolean available, User owner,
                                  ItemRequest itemRequest) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(itemRequest);
        return item;
    }

    public static CommentDtoFromClient createCommentDtoFromClient(String text) {
        CommentDtoFromClient commentDto = new CommentDtoFromClient();
        commentDto.setText(text);
        return commentDto;
    }

    public static CommentDtoToClient createCommentDtoToClient(Long id, String text, LocalDateTime created,
                                                              String authorName) {
        CommentDtoToClient commentDto = new CommentDtoToClient();
        commentDto.setId(id);
        commentDto.setText(text);
        commentDto.setCreated(created);
        commentDto.setAuthorName(authorName);
        return commentDto;
    }

    public static BookingDtoToClient createBookingDtoToClient(Long id, Long bookerId) {
        BookingDtoToClient bookingDto = new BookingDtoToClient();
        bookingDto.setId(id);
        bookingDto.setBookerId(bookerId);
        return bookingDto;
    }

    public static Comment copyOf(Comment comment) {
        Comment copy = new Comment();
        copy.setId(comment.getId());
        copy.setText(comment.getText());
        copy.setCreated(comment.getCreated());
        copy.setAuthor(UserFactory.copyOf(comment.getAuthor()));
        copy.setItem(copyOf(comment.getItem()));
        return copy;
    }

    public static Comment createComment(Long id, String text, LocalDateTime created, User author, Item item) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setText(text);
        comment.setCreated(created);
        comment.setAuthor(author);
        comment.setItem(item);
        return comment;
    }
}
