package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
class CommentDtoFromClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fromJson_withNotEmptyText_shouldReturnDtoWithNotEmptyText() throws JsonProcessingException {
        String validText = "commentText";
        CommentDtoFromClient target = objectMapper.readValue("{\"text\":\"" + validText + "\"}",
                CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", equalTo(validText)));
    }

    @Test
    void fromJson_withEmptyText_shouldReturnDtoWithEmptyText() throws JsonProcessingException {
        CommentDtoFromClient target = objectMapper.readValue("{\"text\":\"\"}",
                CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", is(emptyString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"text\":null}"})
    void fromJson_withNoOrNullText_shouldReturnDtoWithNullText(String source)
            throws JsonProcessingException {
        CommentDtoFromClient target = objectMapper.readValue(source, CommentDtoFromClient.class);

        assertThat(target, hasProperty("text", is(nullValue())));
    }
}