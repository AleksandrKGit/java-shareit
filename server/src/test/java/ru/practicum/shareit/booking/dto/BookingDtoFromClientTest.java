package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import java.time.LocalDateTime;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
class BookingDtoFromClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fromJson_withNotNullFields_shouldReturnDtoWithNotNullFields() throws JsonProcessingException {
        LocalDateTime validStart = LocalDateTime.now().plusMinutes(1);
        LocalDateTime validEnd = validStart.plusMinutes(1);
        Long validItemId = 1L;
        BookingDtoFromClient target = objectMapper.readValue("{\"itemId\":\"" + validItemId + "\", "
                        + "\"start\":\"" + validStart + "\", \"end\":\"" + validEnd + "\"}",
                BookingDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("itemId", equalTo(validItemId)),
                hasProperty("start", equalTo(validStart)),
                hasProperty("end", equalTo(validEnd))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"itemId\":null, \"start\":null, \"end\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source)
            throws JsonProcessingException {
        BookingDtoFromClient target = objectMapper.readValue(source, BookingDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("itemId", is(nullValue())),
                hasProperty("start", is(nullValue())),
                hasProperty("end", is(nullValue()))
        ));
    }
}