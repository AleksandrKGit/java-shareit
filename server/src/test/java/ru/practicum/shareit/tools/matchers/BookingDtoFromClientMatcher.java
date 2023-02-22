package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import java.util.Objects;

public class BookingDtoFromClientMatcher implements ArgumentMatcher<BookingDtoFromClient> {
    private final BookingDtoFromClient dto;

    private BookingDtoFromClientMatcher(BookingDtoFromClient dto) {
        this.dto = dto;
    }

    public static BookingDtoFromClientMatcher equalToDto(BookingDtoFromClient dto) {
        return new BookingDtoFromClientMatcher(dto);
    }

    @Override
    public boolean matches(BookingDtoFromClient dto) {
        return dto != null && this.dto != null
                && Objects.equals(this.dto.getItemId(), dto.getItemId())
                && Objects.equals(this.dto.getStart(), dto.getStart())
                && Objects.equals(this.dto.getEnd(), dto.getEnd());
    }
}