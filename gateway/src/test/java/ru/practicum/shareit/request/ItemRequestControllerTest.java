package ru.practicum.shareit.request;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.practicum.shareit.controller.ControllerErrorHandler;
import ru.practicum.shareit.tools.configuration.AppTestConfiguration;
import java.util.Map;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.tools.factories.DtoFactory.createItemRequestDtoFromClient;
import static ru.practicum.shareit.tools.matchers.ItemRequestDtoFromClientMatcher.equalToDto;

@WebMvcTest(ItemRequestController.class)
@SpringJUnitConfig({AppTestConfiguration.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequestControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ItemRequestClient client;

    @SpyBean
    ControllerErrorHandler controllerErrorHandler;

    @SpyBean
    LocalValidatorFactoryBean validator;

    final Map<String, String> mockedErrors = Map.of("error", "mocked");

    static final long validUserId = 1L;

    static final long validId = 2L;

    static final String validItemRequestJson = "{\"description\":\"text\"}";

    final ItemRequestDtoFromClient validDto = createItemRequestDtoFromClient("description");

    final ResponseEntity<Object> result = ResponseEntity.ok("OK");

    private static Stream<Arguments> httpMediaTypeNotSupportedRequests() {
        return Stream.of(
                Arguments.of("create without media type",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemRequestJson)),

                Arguments.of("create with incorrect media type",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(validItemRequestJson)
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

    private static Stream<Arguments> missingXSharerUserIdHeaderRequests() {
        return Stream.of(
                Arguments.of("create",
                        post("/requests")
                                .content(validItemRequestJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByUser",
                        get("/requests")),

                Arguments.of("readAll",
                        get("/requests/all")),

                Arguments.of("readById",
                        get("/requests/" + validId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingXSharerUserIdHeaderRequests")
    void request_withMissingXSharerUserIdHeader_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleMissingRequestHeaderException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> httpMessageNotReadableRequests() {
        String incorrectFrom = "a";
        String incorrectSize = "a";
        String incorrectUserId = "a";
        String incorrectId = "a";
        String incorrectJson = "}";

        return Stream.of(
                Arguments.of("create with incorrect userId",
                        post("/requests")
                                .header("X-Sharer-User-Id", incorrectUserId)
                                .content(validItemRequestJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("create with incorrect json",
                        post("/requests")
                                .header("X-Sharer-User-Id", validUserId)
                                .content(incorrectJson)
                                .contentType(MediaType.APPLICATION_JSON)),

                Arguments.of("readByUser with incorrect userId",
                        get("/requests")
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readAll with incorrect userId",
                        get("/requests/all")
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readAll with incorrect size",
                        get("/requests/all?size=" + incorrectSize)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readAll with incorrect from",
                        get("/requests/all?from=" + incorrectFrom)
                                .header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readById with incorrect user id",
                        get("/requests/" + validId)
                                .header("X-Sharer-User-Id", incorrectUserId)),

                Arguments.of("readById with incorrect id",
                        get("/requests/" + incorrectId)
                                .header("X-Sharer-User-Id", validUserId))
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

    private static Stream<Arguments> constraintViolationRequests() {
        return Stream.of(
                Arguments.of("readAll with negative from",
                        get("/requests/all?from=-1").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readAll with zero size",
                        get("/requests/all?size=0").header("X-Sharer-User-Id", validUserId)),

                Arguments.of("readAll with negative size",
                        get("/requests/all?size=-1").header("X-Sharer-User-Id", validUserId))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("constraintViolationRequests")
    void request_withInvalidParams_shouldReturnStatusBadRequestAndJsonWithErrors(
            String testName, MockHttpServletRequestBuilder request) throws Exception {
        doReturn(mockedErrors).when(controllerErrorHandler).handleConstraintViolationException(any());

        mockMvc.perform(request)
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPointAndInvalidDto_shouldReturnStatusBadRequestAndJsonWithErrors()
            throws Exception {
        ItemRequestDtoFromClient invalidDto = createItemRequestDtoFromClient(null);
        doReturn(mockedErrors).when(controllerErrorHandler).handleMethodArgumentNotValidException(any());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockedErrors)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void request_withCreateEndPoint_shouldReturnClientCreateMethodResult()
            throws Exception {
        doReturn(result).when(client).create(eq(validUserId), argThat(equalToDto(validDto)));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", validUserId)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());

        verify(validator, times(1)).validate(argThat(equalToDto(validDto)), (Errors) any());
    }

    @Test
    void request_withReadByUserEndPoint_shouldReturnClientReadByUserMethodResult()
            throws Exception {
        doReturn(result).when(client).readByUser(validUserId);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadAllEndPoint_shouldReturnClientReadAllMethodResult()
            throws Exception {
        Integer validFrom = 2;
        Integer validSize = 10;
        doReturn(result).when(client).readAll(validUserId, validFrom, validSize);

        mockMvc.perform(get("/requests/all?from=" + validFrom + "&size=" + validSize)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }

    @Test
    void request_withReadByIdEndPoint_shouldReturnClientReadByIdMethodResult()
            throws Exception {
        doReturn(result).when(client).readById(validUserId, validId);

        mockMvc.perform(get("/requests/" + validId)
                        .header("X-Sharer-User-Id", validUserId))
                .andExpect(content().string("OK"))
                .andExpect(status().isOk());
    }
}