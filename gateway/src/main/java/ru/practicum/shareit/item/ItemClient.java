package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.QueryParameters;
import ru.practicum.shareit.item.dto.CommentDtoFromClient;
import ru.practicum.shareit.item.dto.ItemDtoFromClient;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ItemDtoFromClient requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> createComment(long userId, long itemId, CommentDtoFromClient requestDto) {
        return post("/" + itemId + "/comment", userId, requestDto);
    }

    public ResponseEntity<Object> readByOwner(long userId, Integer from, Integer size) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("size", size);
        queryParameters.add("from", from);
        return get(queryParameters.getQuery(), userId, queryParameters.getParameters());
    }

    public ResponseEntity<Object> readByQuery(long userId, String text, Integer from, Integer size) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("text", text);
        queryParameters.add("from", from);
        queryParameters.add("size", size);
        return get("/search" + queryParameters.getQuery(), userId, queryParameters.getParameters());
    }

    public ResponseEntity<Object> readById(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> update(long userId, Long itemId, ItemDtoFromClient requestDto) {
        return patch("/" + itemId, userId, requestDto);
    }
}