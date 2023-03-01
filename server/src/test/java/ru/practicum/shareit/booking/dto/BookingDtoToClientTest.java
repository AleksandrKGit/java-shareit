package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import static ru.practicum.shareit.tools.factories.BookingFactory.*;

@JsonTest
class BookingDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFields() throws JsonProcessingException, JSONException {
        BookingDtoToClient source = createBookingDtoToClient(null,null, null, null, null,
                null);
        String expected = "{\"id\": null, \"start\": null, \"end\": null, \"status\": null, "
                + "\"item\": null, \"booker\": null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        BookingDtoToClient source = createBookingDtoToClient(1L, LocalDateTime.of(2020, 10,
                        20, 12, 30, 40), LocalDateTime.of(2020, 10,
                        21, 12, 30, 40), BookingStatus.WAITING,
                createItemDtoToClient(2L, "item"), createBookerDtoToClient(3L));
        String expected = "{\"id\":1, \"start\":\"2020-10-20T12:30:40\", \"end\":\"2020-10-21T12:30:40\", "
                + "\"status\":\"WAITING\", \"item\":{\"id\":2, \"name\":\"item\"}, \"booker\":{\"id\":3}}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}