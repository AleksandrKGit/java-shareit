package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import static ru.practicum.shareit.tools.factories.ItemFactory.createBookingDtoToClient;

@JsonTest
class BookingDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFields() throws JsonProcessingException, JSONException {
        BookingDtoToClient source = createBookingDtoToClient(null,null);
        String expected = "{\"id\":null, \"bookerId\":null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        BookingDtoToClient source = createBookingDtoToClient(3L, 4L);
        String expected = "{\"id\":3, \"bookerId\":4}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}