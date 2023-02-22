package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@JsonTest
@SpringJUnitConfig({AppTestConfiguration.class})
class UserDtoFromClientTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fromJson_withNotEmptyFields_shouldReturnDtoWithNotEmptyFields() throws JsonProcessingException {
        String validName = "userName";
        String validEmail = "user@email.com";
        UserDtoFromClient target = objectMapper.readValue("{\"name\":\"" + validName + "\", "
                        + "\"email\":\"" + validEmail + "\"}", UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", equalTo(validName)),
                hasProperty("email", equalTo(validEmail))
        ));
    }

    @Test
    void fromJson_withEmptyFields_shouldReturnDtoWithEmptyFields() throws JsonProcessingException {
        UserDtoFromClient target = objectMapper.readValue("{\"name\":\"\", \"email\":\"\"}",
                UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(emptyString())),
                hasProperty("email", is(emptyString()))
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"name\":null, \"email\":null}"})
    void fromJson_withNoOrNullFields_shouldReturnDtoWithNullFields(String source) throws JsonProcessingException {
        UserDtoFromClient target = objectMapper.readValue(source, UserDtoFromClient.class);

        assertThat(target, allOf(
                hasProperty("name", is(nullValue())),
                hasProperty("email", is(nullValue()))
        ));
    }
}