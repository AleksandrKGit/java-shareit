package ru.practicum.shareit.request.dto;

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
class ItemRequestDtoFromClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fromJson_withNotEmptyDescription_shouldReturnDtoWithNotEmptyDescription() throws JsonProcessingException {
        String validDescription = "requestDescription";
        ItemRequestDtoFromClient target = objectMapper.readValue("{\"description\":\""
                        + validDescription + "\"}", ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", equalTo(validDescription)));
    }

    @Test
    void fromJson_withEmptyDescription_shouldReturnDtoWithEmptyDescription() throws JsonProcessingException {
        ItemRequestDtoFromClient target = objectMapper.readValue("{\"description\":\"\"}",
                ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", is(emptyString())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"description\":null}"})
    void fromJson_withNoOrNullDescription_shouldReturnDtoWithNullDescription(String source)
            throws JsonProcessingException {
        ItemRequestDtoFromClient target = objectMapper.readValue(source, ItemRequestDtoFromClient.class);

        assertThat(target, hasProperty("description", is(nullValue())));
    }
}