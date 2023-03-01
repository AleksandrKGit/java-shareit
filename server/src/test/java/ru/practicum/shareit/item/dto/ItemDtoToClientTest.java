package ru.practicum.shareit.item.dto;

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
import static ru.practicum.shareit.tools.factories.ItemFactory.*;

@JsonTest
class ItemDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFieldsExceptRequestId()
            throws JsonProcessingException, JSONException {
        ItemDtoToClient source = createItemDtoToClient(null,null, null, null, null,
                null, null, null);
        String expected = "{\"id\": null, \"name\": null, \"description\": null, \"available\": null, "
                + "\"lastBooking\": null, \"nextBooking\": null, \"comments\": null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        ItemDtoToClient source = createItemDtoToClient(3L, "name", "description", true,
                4L, createBookingDtoToClient(5L, 6L), createBookingDtoToClient(7L, 8L),
                List.of(createCommentDtoToClient(9L, "comment", LocalDateTime.of(2020, 10,
                        20, 12, 30, 40), "author")));
        String expected = "{\"id\":3, \"name\":\"name\", \"description\":\"description\", \"available\":true, "
                + "\"requestId\":4, \"lastBooking\":{\"id\":5, \"bookerId\":6}, \"nextBooking\":{\"id\":7, "
                + "\"bookerId\":8}, \"comments\":[{\"id\":9, \"text\":\"comment\", "
                + "\"created\":\"2020-10-20 12:30:40\", \"authorName\":\"author\"}]}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}