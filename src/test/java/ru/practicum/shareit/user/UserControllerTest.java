package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.common.ControllerErrorHandler;
import ru.practicum.shareit.tools.configurations.AppTestConfiguration;
import ru.practicum.shareit.user.dto.UserDtoFromClient;
import ru.practicum.shareit.user.dto.UserDtoToClient;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.groups.OnCreate;
import ru.practicum.shareit.validation.groups.OnUpdate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.UserFactory.createUserDtoFromClient;
import static ru.practicum.shareit.tools.factories.UserFactory.createUserDtoToClient;
import static ru.practicum.shareit.tools.matchers.UserDtoFromClientMatcher.equalToDto;

@WebMvcTest(UserController.class)
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService service;

    @SpyBean
    ControllerErrorHandler controllerErrorHandler;

    @SpyBean
    LocalValidatorFactoryBean validator;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validId = 10L;

    static final String validUserJson = "{\"name\":\"userName\",\"email\":\"user@email.com\"}";

    final UserDtoFromClient validDto = createUserDtoFromClient("userName", "user@email.com");

    final UserDtoToClient resultDto = createUserDtoToClient(1L, "user1", "email1");

    final List<UserDtoToClient> resultListDto = List.of(resultDto);

    private static Stream<Arguments> httpMediaTypeNotSupportedRequests() {
        return Stream.of(
                Arguments.of("create without media type",
                        post("/users")
                                .content(validUserJson)),

                Arguments.of("create with incorrect media type",
                        post("/users")
                                .content(validUserJson)
                                .contentType(MediaType.IMAGE_PNG)),

                Arguments.of("update without media type",
                        patch("/users/" + validId)
                                .content(validUserJson)),

                Arguments.of("update with incorrect media type",
                        patch("/users/" + validId)
                                .content(validUserJson)
                                .contentType(MediaType.IMAGE_PNG))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("httpMediaTypeNotSupportedRequests")
    void request_withNotSupportedMediaType_shouldReturnStatusUnsupportedMediaTypeAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleHttpMediaTypeNotSupportedException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isUnsupportedMediaType());
    }

    private static Stream<Arguments> httpMessageNotReadableRequests() {
        String incorrectId = "a";
        String incorrectJson = "}";

        return Stream.of(
                Arguments.of("create with incorrect json",
                        post("/users")
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readById with incorrect id",
                        get("/users/" + incorrectId)),

                Arguments.of("update with incorrect id",
                        patch("/users/" + incorrectId)
                                .content(validUserJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("update with incorrect json",
                        patch("/users/" + validId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("delete with incorrect Id",
                        delete("/users/" + incorrectId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("httpMessageNotReadableRequests")
    void request_withNotReadableMessage_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleHttpMessageNotReadableException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidRequestBodyRequests() {
        String invalidUserJson = "{\"name\":\"\",\"email\":\"\"}";

        return Stream.of(
                Arguments.of("create with empty name and email",
                        post("/users")
                                .content(invalidUserJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("update with empty name and email",
                        patch("/users/" + validId)
                                .content(invalidUserJson)
                                .contentType(MediaType.APPLICATION_JSON))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequestBodyRequests")
    void request_withInvalidRequestBody_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleMethodArgumentNotValidException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPoint_shouldReturnStatusOkAndServiceCreateMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).create(argThat(equalToDto(validDto)));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());

        verify(validator, times(1))
                .validate(argThat(equalToDto(validDto)), (Errors) any(), eq(OnCreate.class));
    }

    @Test
    void request_withReadAllEndPoint_shouldReturnStatusOkAndServiceReadAllMethodResult() throws Exception {
        doReturn(resultListDto).when(service).readAll();

        mockMvc.perform(get("/users"))
                .andExpect(content().json(objectMapper.writeValueAsString(resultListDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnStatusOkAndServiceReadByIdMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).readById(validId);

        mockMvc.perform(get("/users/" + validId))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());
    }

    @Test
    void request_withUpdateEndPoint_shouldReturnStatusOkAndServiceUpdateMethodResult()
            throws Exception {
        doReturn(resultDto).when(service).update(eq(validId), argThat(equalToDto(validDto)));

        mockMvc.perform(patch("/users/" + validId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(resultDto)))
                .andExpect(status().isOk());

        verify(validator, times(1))
                .validate(argThat(equalToDto(validDto)), (Errors) any(), eq(OnUpdate.class));
    }

    @Test
    void request_withDeleteEndPoint_shouldReturnStatusOkAndInvokeServiceDeleteMethod()
            throws Exception {
        mockMvc.perform(delete("/users/" + validId))
                .andExpect(status().isOk());

        verify(service, times(1)).delete(validId);
    }
}