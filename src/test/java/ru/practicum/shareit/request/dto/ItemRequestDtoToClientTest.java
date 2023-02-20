package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import java.time.LocalDateTime;
import java.util.List;
import static ru.practicum.shareit.tools.factories.ItemRequestFactory.*;

@JsonTest
class ItemRequestDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFieldsExceptItems() throws JsonProcessingException, JSONException {
        ItemRequestDtoToClient source = createItemRequestDtoToClient(null,null, null, null);
        String expected = "{\"id\":null, \"description\":null, \"created\":null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        ItemRequestDtoToClient source = createItemRequestDtoToClient(3L, "desc", LocalDateTime.of(2020, 10,
                20, 12, 30, 40), List.of(createItemDtoToClient(
                5L, "item", "desc", true, 3L)));
        String expected = "{\"id\":3, \"description\":\"desc\", \"created\":\"2020-10-20 12:30:40\", \"items\":" +
                "[{\"id\":5, \"name\":\"item\", \"description\":\"desc\", \"available\":true, \"requestId\":3}]}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}