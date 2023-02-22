package ru.practicum.shareit.client;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryParameters {
    private final Map<String, Object> parameters;

    public Map<String, Object> getParameters() {
        return parameters.size() == 0 ? null : parameters;
    }

    private String query;

    public String getQuery() {
        return query.isEmpty() ? "" : "?" + query;
    }

    public QueryParameters() {
        parameters = new HashMap<>();
        query = "";
    }

    public void add(String name, @Nullable Object value) {
        if (value != null) {
            parameters.put(name, value);
            query = (query.isEmpty() ? "" : query + "&") + name + "={" + name + "}";
        }
    }
}