package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDtoFromClient;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.QueryParameters;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, BookingDtoFromClient requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> readByBooker(long userId, BookingState state, Integer from, Integer size) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("state", state);
        queryParameters.add("size", size);
        queryParameters.add("from", from);
        return get(queryParameters.getQuery(), userId, queryParameters.getParameters());
    }

    public ResponseEntity<Object> readByOwner(long userId, BookingState state, Integer from, Integer size) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("state", state);
        queryParameters.add("size", size);
        queryParameters.add("from", from);
        return get("/owner" + queryParameters.getQuery(), userId, queryParameters.getParameters());
    }

    public ResponseEntity<Object> readById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> approve(long userId, Long bookingId, boolean approved) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("approved", approved);
        return patch("/" + bookingId + queryParameters.getQuery(), userId, queryParameters.getParameters(),
                null);
    }
}