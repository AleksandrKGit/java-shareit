package ru.practicum.shareit.tools.factories;

import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;
import ru.practicum.shareit.request.ItemRequestDtoFromClient;
import ru.practicum.shareit.user.UserDtoFromClient;
import java.time.LocalDateTime;

public class DtoFactory {
    public static BookingDtoFromClient createBookingDtoFromClient(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDtoFromClient bookingDto = new BookingDtoFromClient();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        return bookingDto;
    }

    public static ItemDtoFromClient createItemDtoFromClient(String name, String description, Boolean available,
                                                            Long requestId) {
        ItemDtoFromClient itemDto = new ItemDtoFromClient();
        itemDto.setName(name);
        itemDto.setDescription(description);
        itemDto.setAvailable(available);
        itemDto.setRequestId(requestId);
        return itemDto;
    }

    public static ItemRequestDtoFromClient createItemRequestDtoFromClient(String description) {
        ItemRequestDtoFromClient itemRequestDto = new ItemRequestDtoFromClient();
        itemRequestDto.setDescription(description);
        return itemRequestDto;
    }

    public static UserDtoFromClient createUserDtoFromClient(String name, String email) {
        UserDtoFromClient dto = new UserDtoFromClient();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }

    public static CommentDtoFromClient createCommentDtoFromClient(String text) {
        CommentDtoFromClient commentDto = new CommentDtoFromClient();
        commentDto.setText(text);
        return commentDto;
    }
}