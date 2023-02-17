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
import static ru.practicum.shareit.tools.factories.ItemFactory.*;

@JsonTest
class CommentDtoToClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void toJson_withNullFields_shouldReturnJsonStringWithNullFieldsExceptRequestId()
            throws JsonProcessingException, JSONException {
        CommentDtoToClient source = createCommentDtoToClient(null,null, null, null);
        String expected = "{\"id\": null, \"text\": null, \"authorName\": null, \"created\": null}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }

    @Test
    void toJson_withNotNullFields_shouldReturnCorrectJsonString() throws JsonProcessingException, JSONException {
        CommentDtoToClient source = createCommentDtoToClient(3L, "txt", LocalDateTime.of(2020,
                        10,20, 12, 30, 40), "user");
        String expected = "{\"id\":3, \"text\":\"txt\", \"created\":\"2020-10-20 12:30:40\", \"authorName\":\"user\"}";

        String actual = objectMapper.writeValueAsString(source);

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    }
}