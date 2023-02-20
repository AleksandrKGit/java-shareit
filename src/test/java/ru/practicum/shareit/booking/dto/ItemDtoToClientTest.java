package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import static ru.practicum.shareit.tools.factories.BookingFactory.createItemDtoToClient;

@JsonTest
class ItemDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFields() throws JsonProcessingException, JSONException {
        ItemDtoToClient source = createItemDtoToClient(null, null);
        String expected = "{\"id\":null, \"name\":null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        ItemDtoToClient source = createItemDtoToClient(1L,"item");
        String expected = "{\"id\":1, \"name\":\"item\"}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}