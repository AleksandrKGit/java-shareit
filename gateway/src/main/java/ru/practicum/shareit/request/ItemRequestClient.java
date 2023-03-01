package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.client.QueryParameters;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> create(long userId, ItemRequestDtoFromClient requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> readByUser(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> readAll(long userId, Integer from, Integer size) {
        QueryParameters queryParameters = new QueryParameters();
        queryParameters.add("size", size);
        queryParameters.add("from", from);
        return get("/all" + queryParameters.getQuery(), userId, queryParameters.getParameters());
    }

    public ResponseEntity<Object> readById(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }
}