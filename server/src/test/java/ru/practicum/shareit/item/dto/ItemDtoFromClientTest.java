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

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
class ItemDtoFromClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fromJson_withNotEmptyFields_shouldReturnDtoWithNotEmptyFields() throws JsonProcessingException {
        String validName = "itemName";
        String validDescription = "item@description.com";
        Boolean validAvailable = true;
        Long validRequestId = 1L;
        ItemDtoFromClient target = objectMapper.readValue("{\"name\":\"" + validName + "\", "
                + "\"description\":\"" + validDescription + "\", \"available\":" + validAvailable + ", "
                + "\"requestId\":" + validRequestId + "}", ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", equalTo(validName)),
                hasProperty("description", equalTo(validDescription)),
                hasProperty("available", equalTo(validAvailable)),
                hasProperty("requestId", equalTo(validRequestId))
        ));
    }

    @Test
    void fromJson_withEmptyFields_shouldReturnDtoWithEmptyFields() throws JsonProcessingException {
        ItemDtoFromClient target = objectMapper.readValue("{\"name\":\"\", \"description\":\"\"}",
                ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(emptyString())),
                hasProperty("description", is(emptyString()))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":null, \"description\":null, \"available\":null, \"requestId\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source) throws JsonProcessingException {
        ItemDtoFromClient target = objectMapper.readValue(source, ItemDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(nullValue())),
                hasProperty("description", is(nullValue())),
                hasProperty("available", is(nullValue())),
                hasProperty("requestId", is(nullValue()))
        ));
    }
}